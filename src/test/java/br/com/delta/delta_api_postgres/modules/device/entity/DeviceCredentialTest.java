package br.com.delta.delta_api_postgres.modules.device.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.*;

class DeviceCredentialTest {
    private static final Instant CREATED = Instant.parse("2026-10-05T15:00:00Z");

    @Test void expirationIsExclusiveAndRevocationIsPermanent() {
        DeviceCredential credential = credential(CREATED.plusSeconds(60));
        assertThat(credential.isValidAt(CREATED.plusSeconds(59))).isTrue();
        assertThat(credential.isValidAt(CREATED.plusSeconds(60))).isFalse();
        credential.revoke(CREATED.plusSeconds(10));
        credential.revoke(CREATED.plusSeconds(20));
        assertThat(credential.isValidAt(CREATED.plusSeconds(30))).isFalse();
        assertThat(credential.getRevokedAt()).isEqualTo(CREATED.plusSeconds(10));
        assertThat(credential.getCurrentDeviceId()).isNull();
    }

    @Test void optionalExpirationDoesNotRequireCleanupToRemainValid() {
        assertThat(credential(null).isValidAt(CREATED.plusSeconds(86400))).isTrue();
    }

    @Test void secretAndHashAreNotSerializedOrIncludedInErrors() throws Exception {
        var json = new ObjectMapper().findAndRegisterModules().valueToTree(credential(null));
        assertThat(json.has("keyHash")).isFalse();
        assertThat(json.has("currentDeviceId")).isFalse();
        assertThatThrownBy(() -> new DeviceCredential(device(), "secret", CREATED, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Dados da credencial inválidos.");
        assertThatThrownBy(() -> credential(CREATED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private DeviceCredential credential(Instant expiresAt) {
        return new DeviceCredential(device(), "a".repeat(64), CREATED, expiresAt);
    }
    private Device device() {
        Device device = new Device();
        device.setId(7);
        device.setDeviceId("DEVICE-001");
        return device;
    }
}
