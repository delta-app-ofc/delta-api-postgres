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
        "spring.datasource.url=jdbc:h2:mem:credential-issuance;MODE=PostgreSQL;NON_KEYWORDS=MONTH;DB_CLOSE_DELAY=-1",
        "device.credentials.ttl=PT1H"
})
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class DeviceCredentialIssuanceTests extends AuthTestConfig {
    private static final Instant NOW = Instant.parse("2026-10-05T15:00:00Z");
    private static final String PATH = "/delta/device/1000/credentials";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired DeviceRepository devices;
    @Autowired DeviceCredentialRepository credentials;
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

    @Test void returnsSecretOnceAndPersistsOnlyHash(CapturedOutput output) throws Exception {
        var response = mvc.perform(post(PATH).header("Authorization", token())
                        .contentType("application/json").content("{\"api_key\":\"client-choice\",\"credential_id\":999}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("device_id").value("001"))
                .andExpect(jsonPath("created_at").value("2026-10-05T15:00:00Z"))
                .andExpect(jsonPath("expires_at").value("2026-10-05T16:00:00Z"))
                .andExpect(jsonPath("key_hash").doesNotExist())
                .andReturn().getResponse();
        var body = json.readTree(response.getContentAsString());
        String key = body.path("api_key").asText();
        assertThat(key).matches("delta_dev_[A-Za-z0-9_-]{43}").isNotEqualTo("client-choice");
        String stored = jdbc.queryForObject("SELECT key_hash FROM tb_device_credential", String.class);
        assertThat(stored).isEqualTo(keys.hash(key)).isNotEqualTo(key);
        assertThat(body.path("credential_id").asInt()).isNotEqualTo(999);
        mvc.perform(get("/delta/device/1000").header("Authorization", token()))
                .andExpect(status().isOk()).andExpect(jsonPath("deviceId").value("001"))
                .andExpect(jsonPath("api_key").doesNotExist()).andExpect(jsonPath("keyHash").doesNotExist());
        assertThat(output).contains("device_credential_issued", "actor_user_id=42")
                .doesNotContain(key, stored);
    }

    @Test void deniesMissingInactiveAndDuplicateDevicesWithoutExposingSecrets() throws Exception {
        mvc.perform(post("/delta/device/999/credentials").header("Authorization", token()))
                .andExpect(status().isNotFound());
        jdbc.update("UPDATE tb_device SET is_active = FALSE WHERE id = 1000");
        mvc.perform(post(PATH).header("Authorization", token())).andExpect(status().isConflict());
        jdbc.update("UPDATE tb_device SET is_active = TRUE WHERE id = 1000");
        mvc.perform(post(PATH).header("Authorization", token())).andExpect(status().isCreated());
        mvc.perform(post(PATH).header("Authorization", token()))
                .andExpect(status().isConflict()).andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("api_key").doesNotExist()).andExpect(jsonPath("key_hash").doesNotExist());
        assertThat(credentials.count()).isEqualTo(1);
    }

    @Test void requiresExistingUserAuthenticationAndRejectsDeviceKeys() throws Exception {
        mvc.perform(post(PATH)).andExpect(status().isUnauthorized());
        mvc.perform(post(PATH).header("Authorization", "Bearer " + keys.generate()))
                .andExpect(status().isUnauthorized());
        String disabledUserToken = token();
        when(users.existsByIdAndEnabledTrue(42)).thenReturn(false);
        mvc.perform(post(PATH).header("Authorization", disabledUserToken)).andExpect(status().isUnauthorized());
        assertThat(credentials.count()).isZero();
    }

    @Test void zeroTtlIssuesCredentialWithoutExpiration() throws Exception {
        Duration previous = credentialProperties.getTtl();
        credentialProperties.setTtl(Duration.ZERO);
        try {
            var response = mvc.perform(post(PATH).header("Authorization", token()))
                    .andExpect(status().isCreated()).andReturn().getResponse();
            assertThat(json.readTree(response.getContentAsString()).path("expires_at").isNull()).isTrue();
            assertThat(credentials.findByCurrentDeviceId(1000).orElseThrow().getExpiresAt()).isNull();
        } finally {
            credentialProperties.setTtl(previous);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void allowsNewIssuanceAfterExpirationOrRevocation(boolean revoked) throws Exception {
        var old = expiredCredential();
        if (revoked) old.revoke(NOW.minusSeconds(300));
        credentials.saveAndFlush(old);
        mvc.perform(post(PATH).header("Authorization", token())).andExpect(status().isCreated());
        assertThat(credentials.count()).isEqualTo(2);
        assertThat(credentials.findById(old.getId()).orElseThrow().getRevokedAt()).isNotNull();
        assertThat(credentials.findByCurrentDeviceId(1000).orElseThrow().getId()).isNotEqualTo(old.getId());
    }

    @Test void failureRollsBackRetirementOfExpiredCredential(CapturedOutput output) throws Exception {
        var old = credentials.saveAndFlush(expiredCredential());
        doThrow(new IllegalStateException("Falha simulada.")).when(keys).generate();
        mvc.perform(post(PATH).header("Authorization", token())).andExpect(status().isInternalServerError());
        assertThat(credentials.count()).isEqualTo(1);
        assertThat(credentials.findById(old.getId()).orElseThrow().getRevokedAt()).isNull();
        assertThat(credentials.findByCurrentDeviceId(1000)).isPresent();
        assertThat(output).doesNotContain("device_credential_issued");
    }

    @Test void concurrentIssuanceProducesOnlyOneCredential() throws Exception {
        String token = token();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<Integer> request = () -> {
            start.await();
            return mvc.perform(post(PATH).header("Authorization", token)).andReturn().getResponse().getStatus();
        };
        try {
            Future<Integer> first = executor.submit(request);
            Future<Integer> second = executor.submit(request);
            start.countDown();
            assertThat(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(201, 409);
            assertThat(credentials.count()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private DeviceCredential expiredCredential() {
        return new DeviceCredential(devices.findById(1000).orElseThrow(), "a".repeat(64),
                NOW.minusSeconds(7200), NOW.minusSeconds(3600));
    }

    private String token() {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().subject("42").issuer(authProperties.getIssuer())
                .audience(List.of(authProperties.getAudience())).issuedAt(now).expiresAt(now.plusSeconds(300)).build();
        var header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(authProperties.getKeyId()).build();
        return "Bearer " + encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
