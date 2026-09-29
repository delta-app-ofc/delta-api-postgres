package br.com.delta.delta_api_postgres.modules.device.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateDeviceRequest(
        @NotBlank(message = "Device ID é obrigatório")
        String deviceId,
        @NotNull(message = "Property ID é obrigatório")
        Integer propertyId,
        @NotNull(message = "Status do dispositivo é obrigatório")
        Boolean isActive
) {
}
