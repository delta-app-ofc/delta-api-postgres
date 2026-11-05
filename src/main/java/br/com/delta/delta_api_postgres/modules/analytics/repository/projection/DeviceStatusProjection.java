package br.com.delta.delta_api_postgres.modules.analytics.repository.projection;

import java.time.LocalDateTime;

public interface DeviceStatusProjection {
    Integer getOrganizationId();

    Integer getPropertyId();

    String getPropertyName();

    String getRegionName();

    String getDeviceId();

    Boolean getIsActive();

    String getConnectivityStatus();

    Boolean getIsOnline();

    Integer getBatteryLevel();

    Boolean getIsLowBattery();

    String getWifiSignalRssi();

    String getFirmwareVersion();

    LocalDateTime getLastPingAt();

    String getUnavailabilityReason();
}

