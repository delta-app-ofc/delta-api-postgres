package br.com.delta.delta_api_postgres.modules.device.dto.io;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record IssuedDeviceCredentialIO(
        @JsonProperty("credential_id") Integer credentialId,
        @JsonProperty("device_id") String deviceId,
        @JsonProperty("api_key") String apiKey,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("expires_at") Instant expiresAt
) {
    @Override public String toString() {
        return "IssuedDeviceCredentialIO[credentialId=" + credentialId + "]";
    }
}
