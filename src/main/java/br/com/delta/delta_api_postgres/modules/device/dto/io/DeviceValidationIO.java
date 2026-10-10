package br.com.delta.delta_api_postgres.modules.device.dto.io;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DeviceValidationIO(boolean valid, @JsonProperty("device_id") String deviceId) {
    public static DeviceValidationIO invalid() {
        return new DeviceValidationIO(false, null);
    }
}
