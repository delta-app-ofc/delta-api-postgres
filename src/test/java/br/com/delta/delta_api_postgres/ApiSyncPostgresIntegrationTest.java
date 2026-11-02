package br.com.delta.delta_api_postgres;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

/** HTTP real entre as APIs e PostgreSQL/Mongo locais descartaveis; nunca usa Render. */
@EnabledIfSystemProperty(named="delta.test.sync", matches="true")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiSyncPostgresIntegrationTest extends UserDataIntegrationTests {
    @LocalServerPort int sqlPort;
    @Autowired Environment environment;
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        String url=System.getProperty("delta.test.postgres-url", "");
        if (!url.matches("jdbc:postgresql://127[.]0[.]0[.]1:[0-9]+/delta_test_sync")) throw new IllegalArgumentException("Somente PostgreSQL local descartavel");
        registry.add("spring.datasource.url", ()->url);
        registry.add("spring.datasource.username", ()->"delta_test");
        registry.add("spring.datasource.password", ()->"local-test-only");
        registry.add("spring.datasource.driver-class-name", ()->"org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", ()->"org.hibernate.dialect.PostgreSQLDialect");
    }
    @Test void synchronizesActualHttpIngestionOwnerJwtVisitAndDuplicate() throws Exception {
        String mongo=System.getProperty("delta.test.mongo-uri", "");
        if (!mongo.matches("mongodb://127[.]0[.]0[.]1:[0-9]+")) throw new IllegalArgumentException("Somente Mongo local descartavel");
        TestTransaction.flagForCommit(); TestTransaction.end();
        int port;
        try(var socket=new java.net.ServerSocket(0)){port=socket.getLocalPort();}
        var directory=Files.createTempDirectory("delta-api-sync-");
        var publicKey=Path.of(URI.create(environment.getRequiredProperty("auth.public-key"))).toString();
        var args=new ArrayList<String>(List.of(Path.of(System.getProperty("java.home"),"bin",System.getProperty("os.name").startsWith("Windows")?"java.exe":"java").toString(),"-Xmx256m","-jar",
            Path.of(System.getProperty("delta.test.mongo-jar")).toAbsolutePath().toString(),
            "--server.port="+port,"--delta.mongodb.telemetry.uri="+mongo,"--delta.mongodb.app.uri="+mongo,
            "--delta.detection.enabled=true","--delta.alerts.enabled=true",
            "--delta.detection.owner.url=http://127.0.0.1:"+sqlPort,
            "--delta.detection.owner.timeout-ms=10000",
            "--delta.detection.owner.service-key=test-service-key","--delta.alerts.service-key=test-alert-key",
            "--delta.alerts.auth.public-key="+publicKey));
        Process child=new ProcessBuilder(args).directory(directory.toFile()).redirectErrorStream(true)
            .redirectOutput(directory.resolve("mongo-api.log").toFile()).start();
        var http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        String base="http://127.0.0.1:"+port;
        try {
            boolean ready=false;
            for(int attempt=0;attempt<180 && child.isAlive();attempt++) {
                try {ready=http.send(HttpRequest.newBuilder(URI.create(base+"/health")).timeout(Duration.ofSeconds(2)).GET().build(),HttpResponse.BodyHandlers.ofString()).statusCode()==200;} catch(Exception ignored) {}
                if(ready)break; Thread.sleep(1000);
            }
            assertThat(ready).as("Mongo API iniciou; log: %s",directory).isTrue();
            String device=aliceDevice.getDeviceId();
            var owner=http.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+sqlPort+"/delta/internal/device/"+device+"/owner"))
                .timeout(Duration.ofSeconds(20)).header("X-Service-Key","test-service-key").GET().build(),HttpResponse.BodyHandlers.ofString());
            assertThat(owner.statusCode()).as(owner.body()).isEqualTo(200);
            assertThat(json.readTree(owner.body()).path("propertyId").asInt()).isEqualTo(aliceProperty.getId());
            assertThat(json.readTree(owner.body()).path("cohortKey").asText()).contains("RESIDENCIAL");
            Instant end=Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
            String payload="";
            for(int i=7;i>0;i--) {
                Instant start=end.minusSeconds(i*300L);
                payload=json.writeValueAsString(Map.of("device_id",device,"window_started_at",start.toString(),
                    "window_finished_at",start.plusSeconds(300).toString(),"consumption_liters",1.0,"lpm_average",0.2));
                var result=http.send(HttpRequest.newBuilder(URI.create(base+"/api/telemetry/consumption-summary")).timeout(Duration.ofSeconds(15))
                    .header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(payload)).build(),HttpResponse.BodyHandlers.ofString());
                assertThat(result.statusCode()).as(result.body()).isEqualTo(201);
            }
            var duplicate=http.send(HttpRequest.newBuilder(URI.create(base+"/api/telemetry/consumption-summary")).header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload)).build(),HttpResponse.BodyHandlers.ofString());
            assertThat(duplicate.statusCode()).isEqualTo(409);
            var visit=http.send(HttpRequest.newBuilder(URI.create(base+"/api/detection/devices/"+device+"/visit"))
                .header("Authorization",bearer).header("Content-Type","application/json")
                .PUT(HttpRequest.BodyPublishers.ofString("{\"visit_until\":\""+end.plusSeconds(86400)+"\"}")).build(),HttpResponse.BodyHandlers.ofString());
            assertThat(visit.statusCode()).as(visit.body()).isEqualTo(200);
            String evidence="";
            for(int i=0;i<45;i++) {
                var result=http.send(HttpRequest.newBuilder(URI.create(base+"/api/detection/devices/"+device+"/evidence?start="+end.minusSeconds(3600)+"&end="+end.plusSeconds(1)))
                    .header("Authorization",bearer).GET().build(),HttpResponse.BodyHandlers.ofString());
                assertThat(result.statusCode()).as(result.body()).isEqualTo(200);
                evidence=result.body(); if(evidence.contains("completed") && evidence.contains("true"))break;Thread.sleep(1000);
            }
            assertThat(json.readTree(evidence).path("content").size()).isEqualTo(7);
            assertThat(evidence).contains("completed","true");
            var alerts=http.send(HttpRequest.newBuilder(URI.create(base+"/api/alerts?size=100"))
                .header("Authorization",bearer).GET().build(),HttpResponse.BodyHandlers.ofString());
            assertThat(alerts.statusCode()).as(alerts.body()).isEqualTo(200);
            int continuousAlerts=0;
            for(var alert:json.readTree(alerts.body()).path("content")) {
                if(device.equals(alert.path("device_id").asText()) && "continuous_flow".equals(alert.path("rule").asText())) {
                    continuousAlerts++;
                    assertThat(alert.path("source").asText()).isEqualTo("detector");
                    assertThat(alert.path("property_id").asInt()).isEqualTo(aliceProperty.getId());
                }
            }
            assertThat(continuousAlerts).isEqualTo(1);
            String bobToken="Bearer "+tokens.accessToken(bob,clock.instant());
            var denied=http.send(HttpRequest.newBuilder(URI.create(base+"/api/detection/devices/"+device+"/evidence?start="+end.minusSeconds(3600)+"&end="+end))
                .header("Authorization",bobToken).GET().build(),HttpResponse.BodyHandlers.ofString());
            assertThat(denied.statusCode()).isEqualTo(404);
        } finally { child.destroy(); if(!child.waitFor(10,java.util.concurrent.TimeUnit.SECONDS))child.destroyForcibly(); }
    }
}
