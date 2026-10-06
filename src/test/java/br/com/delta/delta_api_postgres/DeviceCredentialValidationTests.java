package br.com.delta.delta_api_postgres;

import br.com.delta.delta_api_postgres.modules.auth.config.AuthProperties;
import br.com.delta.delta_api_postgres.modules.auth.repository.AuthUserRepository;
import br.com.delta.delta_api_postgres.modules.device.entity.DeviceCredential;
import br.com.delta.delta_api_postgres.modules.device.config.DeviceCredentialProperties;
import br.com.delta.delta_api_postgres.modules.device.repository.DeviceCredentialRepository;
import br.com.delta.delta_api_postgres.modules.device.repository.DeviceRepository;
import br.com.delta.delta_api_postgres.modules.device.security.DeviceApiKeys;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.*;
import java.util.List;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:credential-validation;MODE=PostgreSQL;NON_KEYWORDS=MONTH;DB_CLOSE_DELAY=-1",
        "device.credentials.ttl=PT1H",
        "device.integration.mongo-credential=delta_svc_mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm"
})
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class DeviceCredentialValidationTests extends AuthTestConfig {
    private static final Instant NOW = Instant.parse("2026-10-05T15:00:00Z");
    private static final String PATH = "/delta/device/1000/credentials";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired DeviceRepository devices;
    @MockitoSpyBean DeviceCredentialRepository credentials;
    @Autowired JwtEncoder encoder;
    @Autowired AuthProperties authProperties;
    @Autowired DeviceCredentialProperties credentialProperties;
    @MockitoSpyBean DeviceApiKeys keys;
    @MockitoBean Clock clock;
    @MockitoBean AuthUserRepository users;

    @BeforeEach void seedExistingDevice() {
        when(clock.instant()).thenReturn(NOW);
        when(users.existsByIdAndEnabledTrue(42)).thenReturn(true);
        jdbc.update("DELETE FROM tb_device_credential");
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
                + "VALUES (1000, '001', 1000, TRUE, DATE '2026-01-01')");
    }

    private static final String VALIDATE_PATH = "/delta/internal/device-auth/validate";
    private static final String SERVICE_KEY = "delta_svc_" + "m".repeat(43);
    @Autowired br.com.delta.delta_api_postgres.modules.device.config.DeviceIntegrationProperties integration;

    @Test void authenticatesServiceAndDerivesCanonicalDeviceIdentity(CapturedOutput output) throws Exception {
        var issued = issue();
        String key = issued.path("api_key").asText();
        String body = json.writeValueAsString(java.util.Map.of("api_key", key, "device_id", "another-device"));
        mvc.perform(post(VALIDATE_PATH).header("Authorization", "Bearer " + SERVICE_KEY)
                        .contentType("application/json").content(body))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("valid").value(true)).andExpect(jsonPath("device_id").value("001"))
                .andExpect(jsonPath("credential_id").value(issued.path("credential_id").asInt()))
                .andExpect(jsonPath("permissions[0]").value("telemetry:write"))
                .andExpect(jsonPath("permissions.length()").value(1))
                .andExpect(jsonPath("api_key").doesNotExist()).andExpect(jsonPath("key_hash").doesNotExist());
        assertThat(output).doesNotContain(key, SERVICE_KEY, keys.hash(key));
    }

    @Test void deviceBlockingIsReversibleAndExpirationIsExclusive() throws Exception {
        String key = issue().path("api_key").asText();
        jdbc.update("UPDATE tb_device SET is_active = FALSE WHERE id = 1000");
        invalid(key);
        jdbc.update("UPDATE tb_device SET is_active = TRUE WHERE id = 1000");
        validate(key).andExpect(jsonPath("valid").value(true));
        when(clock.instant()).thenReturn(NOW.plusSeconds(3600));
        invalid(key);
    }

    @Test void rotationRevocationAndDeletionImmediatelyInvalidateOldKeys() throws Exception {
        String firstKey = issue().path("api_key").asText();
        var rotated = mvc.perform(post(PATH + "/rotate").header("Authorization", token()))
                .andExpect(status().isOk()).andReturn().getResponse();
        String nextKey = json.readTree(rotated.getContentAsString()).path("api_key").asText();
        invalid(firstKey);
        validate(nextKey).andExpect(jsonPath("valid").value(true));
        mvc.perform(delete(PATH + "/current").header("Authorization", token())).andExpect(status().isNoContent());
        invalid(nextKey);
        String lastKey = issue().path("api_key").asText();
        mvc.perform(delete("/delta/device/1000").header("Authorization", token())).andExpect(status().isNoContent());
        invalid(lastKey);
    }

    @Test void invalidStringsHaveUniformResponseWithoutIdentity() throws Exception {
        invalid(keys.generate());
        invalid("");
        invalid("incorrect-format");
        invalid("x".repeat(256));
        invalid(SERVICE_KEY);
    }

    @Test void invalidJsonMissingFieldsWrongTypesAndOversizedKeysReturn400() throws Exception {
        for (String body : List.of("{", "{}", "{\"api_key\":null}", "{\"api_key\":123}",
                "{\"api_key\":true}", "{\"api_key\":[]}", "{\"api_key\":{}}",
                json.writeValueAsString(java.util.Map.of("api_key", "x".repeat(257))))) {
            mvc.perform(post(VALIDATE_PATH).header("Authorization", "Bearer " + SERVICE_KEY)
                            .contentType("application/json").content(body))
                    .andExpect(status().isBadRequest()).andExpect(header().string("Cache-Control", "no-store"));
        }
    }

    @Test void onlyIndependentServiceCredentialCanAuthorizeValidation() throws Exception {
        String key = issue().path("api_key").asText();
        String body = json.writeValueAsString(java.util.Map.of("api_key", key));
        mvc.perform(post(VALIDATE_PATH).contentType("application/json").content(body)).andExpect(status().isUnauthorized());
        for (String authorization : List.of(token(), "Bearer " + key, "Bearer delta_svc_" + "z".repeat(43), "Basic example")) {
            mvc.perform(post(VALIDATE_PATH).header("Authorization", authorization).contentType("application/json").content(body))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post(VALIDATE_PATH).header("Authorization", "Bearer " + SERVICE_KEY, "Bearer " + SERVICE_KEY)
                        .contentType("application/json").content(body)).andExpect(status().isUnauthorized());
        // Authenticate the caller before attempting to parse an invalid body.
        mvc.perform(post(VALIDATE_PATH).contentType("application/json").content("{"))
                .andExpect(status().isUnauthorized());
    }

    @Test void serviceCredentialCannotManageDevicesOrKeysOrUseOtherInternalOperations() throws Exception {
        String header = "Bearer " + SERVICE_KEY;
        mvc.perform(post(PATH).header("Authorization", header)).andExpect(status().isUnauthorized());
        mvc.perform(post(PATH + "/rotate").header("Authorization", header)).andExpect(status().isUnauthorized());
        mvc.perform(delete(PATH + "/current").header("Authorization", header)).andExpect(status().isUnauthorized());
        mvc.perform(delete("/delta/device/1000").header("Authorization", header)).andExpect(status().isUnauthorized());
        mvc.perform(post("/delta/device").header("Authorization", header).contentType("application/json")
                        .content("{\"deviceId\":\"NEW\",\"propertyId\":1000}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(VALIDATE_PATH).header("Authorization", header)).andExpect(status().isForbidden());
        mvc.perform(post("/delta/internal/device-auth/other").header("Authorization", header)).andExpect(status().isForbidden());
        assertThat(credentials.count()).isZero();
        assertThat(devices.findById(1000)).isPresent();
    }

    @Test void missingIntegrationConfigurationDeniesAuthentication() throws Exception {
        String previous = integration.getMongoCredential();
        integration.setMongoCredential("");
        try {
            mvc.perform(post(VALIDATE_PATH).header("Authorization", "Bearer " + SERVICE_KEY)
                            .contentType("application/json").content("{\"api_key\":\"unknown\"}"))
                    .andExpect(status().isUnauthorized());
        } finally {
            integration.setMongoCredential(previous);
        }
    }

    @Test void infrastructureFailureIsNotReportedAsInvalidCredential() throws Exception {
        doThrow(new org.springframework.dao.DataAccessResourceFailureException("Banco indisponível."))
                .when(credentials).findByKeyHash(anyString());
        mvc.perform(post(VALIDATE_PATH).header("Authorization", "Bearer " + SERVICE_KEY)
                        .contentType("application/json").content(json.writeValueAsString(java.util.Map.of("api_key", keys.generate()))))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("valid").doesNotExist())
                .andExpect(jsonPath("device_id").doesNotExist());
    }

    private org.springframework.test.web.servlet.ResultActions validate(String key) throws Exception {
        return mvc.perform(post(VALIDATE_PATH).header("Authorization", "Bearer " + SERVICE_KEY)
                        .contentType("application/json").content(json.writeValueAsString(java.util.Map.of("api_key", key))))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"));
    }
    private void invalid(String key) throws Exception {
        var response = validate(key).andReturn().getResponse();
        assertThat(json.readTree(response.getContentAsString())).isEqualTo(json.readTree("{\"valid\":false}"));
    }
    private com.fasterxml.jackson.databind.JsonNode issue() throws Exception {
        var response = mvc.perform(post(PATH).header("Authorization", token()))
                .andExpect(status().isCreated()).andReturn().getResponse();
        return json.readTree(response.getContentAsString());
    }
    private String token() {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().subject("42").issuer(authProperties.getIssuer())
                .audience(List.of(authProperties.getAudience())).issuedAt(now).expiresAt(now.plusSeconds(300)).build();
        var header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(authProperties.getKeyId()).build();
        return "Bearer " + encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
