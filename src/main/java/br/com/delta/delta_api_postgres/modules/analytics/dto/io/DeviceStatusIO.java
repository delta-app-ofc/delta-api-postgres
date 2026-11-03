package br.com.delta.delta_api_postgres.modules.analytics.dto.io;

import java.time.LocalDateTime;

public record DeviceStatusIO(
        Integer organizationId,
        Integer propertyId,
        String propertyName,
        String regionName,
        String deviceId,
        Boolean isActive,
        String connectivityStatus,
        Boolean isOnline,
        Integer batteryLevel,
        Boolean isLowBattery,
        String wifiSignalRssi,
        String firmwareVersion,
        LocalDateTime lastPingAt,
        String unavailabilityReason
) {
}

