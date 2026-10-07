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
        "spring.datasource.url=jdbc:h2:mem:credential-revocation;MODE=PostgreSQL;NON_KEYWORDS=MONTH;DB_CLOSE_DELAY=-1",
        "device.credentials.ttl=PT1H"
})
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class DeviceCredentialRevocationTests extends AuthTestConfig {
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

    @Test void revocationIsIdempotentPreservesHistoryAndAllowsReissuance(CapturedOutput output) throws Exception {
        var first = issue();
        mvc.perform(delete(PATH + "/current").header("Authorization", token()))
                .andExpect(status().isNoContent()).andExpect(header().string("Cache-Control", "no-store"));
        when(clock.instant()).thenReturn(NOW.plusSeconds(60));
        mvc.perform(delete(PATH + "/current").header("Authorization", token())).andExpect(status().isNoContent());
        var old = credentials.findById(first.path("credential_id").asInt()).orElseThrow();
        assertThat(old.getRevokedAt()).isEqualTo(NOW);
        assertThat(old.isValidAt(NOW.plusSeconds(60))).isFalse();
        assertThat(credentials.findByCurrentDeviceId(1000)).isEmpty();
        assertThat(devices.findById(1000)).isPresent();
        assertThat(credentials.count()).isEqualTo(1);
        mvc.perform(post(PATH + "/rotate").header("Authorization", token())).andExpect(status().isConflict());
        var newCredential = issue();
        assertThat(newCredential.path("credential_id").asInt()).isNotEqualTo(old.getId());
        assertThat(credentials.count()).isEqualTo(2);
        assertThat(output).contains("device_credential_revoked", "actor_user_id=42")
                .doesNotContain(first.path("api_key").asText());
    }

    @Test void canRevokeInactiveAndExpiredDeviceCredentials() throws Exception {
        var first = issue();
        jdbc.update("UPDATE tb_device SET is_active = FALSE WHERE id = 1000");
        when(clock.instant()).thenReturn(NOW.plusSeconds(3600));
        mvc.perform(delete(PATH + "/current").header("Authorization", token())).andExpect(status().isNoContent());
        assertThat(credentials.findById(first.path("credential_id").asInt()).orElseThrow().getRevokedAt())
                .isEqualTo(NOW.plusSeconds(3600));
        assertThat(devices.findById(1000).orElseThrow().isActive()).isFalse();
    }

    @Test void missingDeviceReturns404AndExistingDeviceWithoutKeyReturns204() throws Exception {
        mvc.perform(delete("/delta/device/999/credentials/current").header("Authorization", token()))
                .andExpect(status().isNotFound());
        mvc.perform(delete(PATH + "/current").header("Authorization", token())).andExpect(status().isNoContent());
        assertThat(credentials.count()).isZero();
    }

    @Test void rejectsAnonymousAndDeviceKeyCallers() throws Exception {
        var first = issue();
        mvc.perform(delete(PATH + "/current")).andExpect(status().isUnauthorized());
        mvc.perform(delete(PATH + "/current").header("Authorization", "Bearer " + first.path("api_key").asText()))
                .andExpect(status().isUnauthorized());
        String disabledToken = token();
        when(users.existsByIdAndEnabledTrue(42)).thenReturn(false);
        mvc.perform(delete(PATH + "/current").header("Authorization", disabledToken)).andExpect(status().isUnauthorized());
        assertThat(credentials.findById(first.path("credential_id").asInt()).orElseThrow().getRevokedAt()).isNull();
    }

    @Test void concurrentRevocationsBothReturn204WithoutLosingHistory() throws Exception {
        var first = issue();
        String token = token();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Integer> request = () -> {
            start.await();
            return mvc.perform(delete(PATH + "/current").header("Authorization", token())).andReturn().getResponse().getStatus();
        };
        try {
            Future<Integer> one = executor.submit(request);
            Future<Integer> two = executor.submit(request);
            start.countDown();
            assertThat(List.of(one.get(15, TimeUnit.SECONDS), two.get(15, TimeUnit.SECONDS))).containsOnly(204);
            assertThat(credentials.count()).isEqualTo(1);
            assertThat(credentials.findById(first.path("credential_id").asInt()).orElseThrow().getRevokedAt()).isNotNull();
        } finally {
            executor.shutdownNow();
        }
    }
    @Test void concurrentRotationAndRevocationCannotRestoreARevokedKey() throws Exception {
        issue();
        String token = token();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Integer> rotation = executor.submit(() -> {
                start.await();
                return mvc.perform(post(PATH + "/rotate").header("Authorization", token)).andReturn().getResponse().getStatus();
            });
            Future<Integer> revocation = executor.submit(() -> {
                start.await();
                return mvc.perform(delete(PATH + "/current").header("Authorization", token)).andReturn().getResponse().getStatus();
            });
            start.countDown();
            assertThat(revocation.get(15, TimeUnit.SECONDS)).isEqualTo(204);
            assertThat(rotation.get(15, TimeUnit.SECONDS)).isIn(200, 409);
            assertThat(credentials.findByCurrentDeviceId(1000)).isEmpty();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM tb_device_credential WHERE revoked_at IS NULL", Integer.class)).isZero();
        } finally {
            executor.shutdownNow();
        }
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
