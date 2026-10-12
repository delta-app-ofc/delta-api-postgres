package br.com.delta.delta_api_postgres;

import br.com.delta.delta_api_postgres.modules.auth.config.AuthProperties;
import br.com.delta.delta_api_postgres.modules.auth.repository.AuthUserRepository;
import br.com.delta.delta_api_postgres.modules.device.config.DeviceIntegrationProperties;
import br.com.delta.delta_api_postgres.modules.device.repository.DeviceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:device-validation;MODE=PostgreSQL;NON_KEYWORDS=MONTH;DB_CLOSE_DELAY=-1",
        "device.integration.mongo-credential=delta_svc_mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm"
})
@AutoConfigureMockMvc
class DeviceValidationTests extends AuthTestConfig {
    private static final String PATH = "/delta/internal/devices/validate";
    private static final String SERVICE_KEY = "delta_svc_" + "m".repeat(43);
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired DeviceIntegrationProperties integration;
    @Autowired JwtEncoder encoder;
    @Autowired AuthProperties authProperties;
    @Autowired org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping mappings;
    @MockitoSpyBean DeviceRepository devices;
    @MockitoBean AuthUserRepository users;
    @LocalServerPort int port;

    @BeforeEach void seedExistingDevice() {
        when(users.existsByIdAndEnabledTrue(42)).thenReturn(true);
        jdbc.update("DELETE FROM tb_device");
        jdbc.update("DELETE FROM tb_property");
        jdbc.update("DELETE FROM tb_address");
        jdbc.update("DELETE FROM tb_property_classification");
        jdbc.update("DELETE FROM tb_region");
        jdbc.update("INSERT INTO tb_region (id, name) VALUES (1000, 'GRANDE_SP')");
        jdbc.update("INSERT INTO tb_address (id, region_id, cep, city, state) VALUES (1000, 1000, '01001000', 'São Paulo', 'SP')");
        jdbc.update("INSERT INTO tb_property_classification (id, name, group_name) VALUES (1000, 'RESIDENCIAL_NORMAL', 'RESIDENCIAL')");
        jdbc.update("INSERT INTO tb_property (id, name, type, classification_id, address_id, registration_date) "
                + "VALUES (1000, 'Casa', 'CASA', 1000, 1000, DATE '2026-01-01')");
        jdbc.update("INSERT INTO tb_device (id, device_id, property_id, is_active, installation_date) "
                + "VALUES (1000, 'ESP00321', 1000, TRUE, DATE '2026-01-01')");
    }

    @Test void activeDeviceReturnsOnlyStoredIdentity() throws Exception {
        var response = validate("ESP00321").andReturn().getResponse();
        assertThat(json.readTree(response.getContentAsString()))
                .isEqualTo(json.readTree("{\"valid\":true,\"device_id\":\"ESP00321\"}"));
    }

    @Test void inactiveDeletedAndUnknownDevicesAreInvalidAndReactivationWorks() throws Exception {
        invalid("unknown");
        invalid("1000");
        jdbc.update("UPDATE tb_device SET is_active = FALSE WHERE id = 1000");
        invalid("ESP00321");
        jdbc.update("UPDATE tb_device SET is_active = TRUE WHERE id = 1000");
        validate("ESP00321").andExpect(jsonPath("valid").value(true));
        jdbc.update("DELETE FROM tb_device WHERE id = 1000");
        invalid("ESP00321");
    }

    @Test void identityIsNotNormalizedAndLengthBoundaryIsAccepted() throws Exception {
        invalid("esp00321");
        invalid(" ESP00321 ");
        invalid("ESP321");
        String id = "x".repeat(100);
        jdbc.update("UPDATE tb_device SET device_id = ? WHERE id = 1000", id);
        validate(id).andExpect(jsonPath("device_id").value(id));
    }

    @Test void invalidPayloadsReturn400() throws Exception {
        for (String body : List.of("{", "{}", "{\"device_id\":null}", "{\"device_id\":123}",
                "{\"device_id\":true}", "{\"device_id\":[]}", "{\"device_id\":{}}",
                "{\"device_id\":\"\"}", "{\"device_id\":\"   \"}",
                json.writeValueAsString(Map.of("device_id", "x".repeat(101))))) {
            mvc.perform(post(PATH).header("Authorization", "Bearer " + SERVICE_KEY)
                            .contentType("application/json").content(body))
                    .andExpect(status().isBadRequest()).andExpect(header().string("Cache-Control", "no-store"));
        }
    }

    @Test void onlyServiceCredentialAuthorizesValidation() throws Exception {
        mvc.perform(post(PATH).contentType("application/json").content("{"))
                .andExpect(status().isUnauthorized());
        for (String authorization : List.of(token(), "Bearer delta_svc_" + "z".repeat(43), "Basic example")) {
            mvc.perform(post(PATH).header("Authorization", authorization).contentType("application/json")
                            .content("{\"device_id\":\"ESP00321\"}"))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post(PATH).header("Authorization", "Bearer " + SERVICE_KEY, "Bearer " + SERVICE_KEY)
                        .contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test void serviceCannotManageDevicesOrUseOtherInternalOperations() throws Exception {
        String authorization = "Bearer " + SERVICE_KEY;
        mvc.perform(delete("/delta/device/1000").header("Authorization", authorization))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/delta/device").header("Authorization", authorization).contentType("application/json")
                        .content("{\"deviceId\":\"NEW\",\"propertyId\":1000}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(PATH).header("Authorization", authorization)).andExpect(status().isForbidden());
        mvc.perform(post("/delta/internal/devices/other").header("Authorization", authorization))
                .andExpect(status().isForbidden());
        assertThat(devices.findById(1000)).isPresent();
    }

    @Test void removedCredentialRoutesAndOldValidationHaveNoHandlers() throws Exception {
        assertThat(mappings.getHandlerMethods().keySet().stream()
                .flatMap(mapping -> mapping.getPatternValues().stream()).toList())
                .noneMatch(path -> path.contains("/credentials") || path.contains("/device-auth"));
    }

    @Test void missingIntegrationConfigurationDeniesAccess() throws Exception {
        String previous = integration.getMongoCredential();
        integration.setMongoCredential("");
        try {
            mvc.perform(post(PATH).header("Authorization", "Bearer " + SERVICE_KEY)
                            .contentType("application/json").content("{\"device_id\":\"ESP00321\"}"))
                    .andExpect(status().isUnauthorized());
        } finally {
            integration.setMongoCredential(previous);
        }
    }

    @Test void databaseFailureRemainsServiceFailure() throws Exception {
        doThrow(new org.springframework.dao.DataAccessResourceFailureException("Banco indisponível."))
                .when(devices).findByDeviceId(anyString());
        mvc.perform(post(PATH).header("Authorization", "Bearer " + SERVICE_KEY)
                        .contentType("application/json").content("{\"device_id\":\"ESP00321\"}"))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("valid").doesNotExist())
                .andExpect(jsonPath("device_id").doesNotExist());
    }

    @Test void realHttpValidatesActiveInactiveUnknownAndInvalidPayloads() throws Exception {
        assertHttp("{\"device_id\":\"ESP00321\"}", 200, "{\"valid\":true,\"device_id\":\"ESP00321\"}");
        jdbc.update("UPDATE tb_device SET is_active = FALSE WHERE id = 1000");
        assertHttp("{\"device_id\":\"ESP00321\"}", 200, "{\"valid\":false}");
        assertHttp("{\"device_id\":\"unknown\"}", 200, "{\"valid\":false}");
        assertHttp("{\"device_id\":123}", 400, null);
    }

    private void assertHttp(String body, int status, String expected) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + PATH))
                .header("Authorization", "Bearer " + SERVICE_KEY).header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(10)).POST(HttpRequest.BodyPublishers.ofString(body)).build();
        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(status);
        assertThat(response.headers().firstValue("Cache-Control")).contains("no-store");
        if (expected != null) assertThat(json.readTree(response.body())).isEqualTo(json.readTree(expected));
    }

    private ResultActions validate(String id) throws Exception {
        return mvc.perform(post(PATH).header("Authorization", "Bearer " + SERVICE_KEY)
                        .contentType("application/json").content(json.writeValueAsString(Map.of("device_id", id))))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"));
    }

    private void invalid(String id) throws Exception {
        assertThat(json.readTree(validate(id).andReturn().getResponse().getContentAsString()))
                .isEqualTo(json.readTree("{\"valid\":false}"));
    }

    private String token() {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().subject("42").issuer(authProperties.getIssuer())
                .audience(List.of(authProperties.getAudience())).issuedAt(now).expiresAt(now.plusSeconds(300)).build();
        var header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(authProperties.getKeyId()).build();
        return "Bearer " + encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
