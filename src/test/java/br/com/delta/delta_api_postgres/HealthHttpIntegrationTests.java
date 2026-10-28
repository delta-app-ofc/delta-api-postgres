package br.com.delta.delta_api_postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthHttpIntegrationTests extends AuthTestConfig {
    @LocalServerPort int port;
    @Autowired ObjectMapper json;
    @MockitoBean LettuceConnectionFactory redisFactory;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void returnsUpOverHttpWhenDependenciesRespond() throws Exception {
        RedisConnection redis = mock(RedisConnection.class);
        when(redisFactory.getConnection()).thenReturn(redis);
        when(redis.ping()).thenReturn("PONG");

        HttpResponse<String> response = getHealth();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(json.readTree(response.body()).path("status").asText()).isEqualTo("UP");
    }

    @Test
    void returnsDownOverHttpWhenRedisIsUnavailable() throws Exception {
        when(redisFactory.getConnection()).thenThrow(new RedisConnectionFailureException("redis unavailable"));

        HttpResponse<String> response = getHealth();

        assertThat(response.statusCode()).isEqualTo(503);
        assertThat(json.readTree(response.body()).path("status").asText()).isEqualTo("DOWN");
    }

    private HttpResponse<String> getHealth() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/health"))
                .GET().build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
