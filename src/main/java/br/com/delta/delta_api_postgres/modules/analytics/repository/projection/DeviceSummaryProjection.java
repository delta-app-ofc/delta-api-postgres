package br.com.delta.delta_api_postgres.modules.analytics.repository.projection;

public interface DeviceSummaryProjection {
    Integer getOrganizationId();

    Long getPropertiesTotal();

    Long getDevicesPaired();

    Long getDevicesOnline();

    Long getDevicesLowBattery();

    Long getPropertiesWithoutDevice();
}

