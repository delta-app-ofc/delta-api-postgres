package br.com.delta.delta_api_postgres.modules.device.dto.request;

public record UpdateDeviceRequest(
        String deviceId,
        Integer propertyId,
        Boolean isActive
) {
}
