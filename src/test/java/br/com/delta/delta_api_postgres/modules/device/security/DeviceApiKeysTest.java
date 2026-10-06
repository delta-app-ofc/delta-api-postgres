package br.com.delta.delta_api_postgres.modules.device.security;

import br.com.delta.delta_api_postgres.modules.device.dto.io.IssuedDeviceCredentialIO;
import org.junit.jupiter.api.Test;
import java.util.Base64;
import static org.assertj.core.api.Assertions.*;

class DeviceApiKeysTest {
    private final DeviceApiKeys keys = new DeviceApiKeys();

    @Test void generatesIndependentUrlSafeKeysWith32RandomBytes() {
        String first = keys.generate();
        assertThat(first).matches("delta_dev_[A-Za-z0-9_-]{43}");
        assertThat(Base64.getUrlDecoder().decode(first.substring("delta_dev_".length()))).hasSize(32);
        assertThat(keys.generate()).isNotEqualTo(first);
    }

    @Test void usesSha256OfTheEntireInput() {
        assertThat(keys.hash("abc")).isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        String key = keys.generate();
        assertThat(keys.hash(key)).matches("[0-9a-f]{64}").isNotEqualTo(keys.hash(key.substring(10)));
    }

    @Test void responseToStringDoesNotExposeSecret() {
        String key = keys.generate();
        assertThat(new IssuedDeviceCredentialIO(1, "DEVICE-001", key, null, null).toString())
                .doesNotContain(key).contains("credentialId=1");
    }
}
