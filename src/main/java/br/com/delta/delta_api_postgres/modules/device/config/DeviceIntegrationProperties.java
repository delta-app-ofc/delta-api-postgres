package br.com.delta.delta_api_postgres.modules.device.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "device.integration")
@Validated
@Getter
@Setter
public class DeviceIntegrationProperties {
    // Blank disables service authentication. The distinct prefix prevents use of a device key.
    @NotNull
    private String mongoCredential = "";

    @AssertTrue(message = "Configure uma credencial exclusiva de integração com prefixo delta_svc_.")
    public boolean isMongoCredentialValid() {
        return mongoCredential != null && (mongoCredential.isEmpty() || mongoCredential.matches("delta_svc_[A-Za-z0-9_-]{43,128}"));
    }
}
