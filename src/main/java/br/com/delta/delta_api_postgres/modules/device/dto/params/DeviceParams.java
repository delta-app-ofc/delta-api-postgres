package br.com.delta.delta_api_postgres.modules.device.dto.params;

public record DeviceParams(
        Integer propertyId,
        Boolean isActive
) {
}
