package br.com.delta.delta_api_postgres.dto.params;

public record DeviceParams(
        Integer propertyId,
        Boolean isActive
) {
}
