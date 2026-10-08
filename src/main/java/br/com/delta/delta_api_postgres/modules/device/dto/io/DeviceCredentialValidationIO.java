package br.com.delta.delta_api_postgres.modules.device.dto.io;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DeviceCredentialValidationIO(boolean valid,
        @JsonProperty("device_id") String deviceId,
        @JsonProperty("credential_id") Integer credentialId, List<String> permissions) {
    public static DeviceCredentialValidationIO invalid() {
        return new DeviceCredentialValidationIO(false, null, null, null);
    }
}
