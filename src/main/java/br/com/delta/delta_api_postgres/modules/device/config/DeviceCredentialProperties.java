package br.com.delta.delta_api_postgres.modules.device.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "device.credentials")
@Validated
@Getter
@Setter
public class DeviceCredentialProperties {
    @NotNull
    private Duration ttl = Duration.ZERO;

    @AssertTrue(message = "TTL deve ser zero (sem expiração) ou pelo menos um segundo.")
    public boolean isTtlValid() {
        return ttl != null && (ttl.isZero() || ttl.compareTo(Duration.ofSeconds(1)) >= 0);
    }
}
