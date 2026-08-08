package br.com.delta.delta_api_postgres.dto.request;

public record CreateDeviceRequest(
        String deviceId,
        Integer propertyId,
        Boolean isActive
) {
}
