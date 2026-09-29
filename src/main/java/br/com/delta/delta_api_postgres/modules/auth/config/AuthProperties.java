package br.com.delta.delta_api_postgres.modules.auth.config;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;
import java.util.List;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    @NotBlank private String issuer = "delta-api";
    @NotBlank private String audience = "delta-api";
    @NotBlank private String keyId = "delta-1";
    private String privateKey = "";
    private String publicKey = "";
    private List<String> allowedOrigins = List.of("http://localhost:5173", "http://localhost:3000");
    @Min(1) private int attemptsPerMinute = 30;
    @NotNull private Duration refreshTtl = Duration.ofDays(30);
}
