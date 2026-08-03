package br.com.delta.delta_api_postgres.dto.request;

public record CreateDeviceRequest(
        Integer deviceId,
        Integer propertyId,
        Boolean isActive
) {
}
