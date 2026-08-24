package br.com.delta.delta_api_postgres.modules.device.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDeviceRequest(
        @NotBlank(message = "Device ID é obrigatorio")
        String deviceId,
        @NotNull(message = "Property ID é obrigatório")
        Integer propertyId,
        Boolean isActive
) {
}
