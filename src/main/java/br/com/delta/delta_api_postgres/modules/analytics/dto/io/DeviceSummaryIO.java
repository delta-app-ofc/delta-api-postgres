package br.com.delta.delta_api_postgres.modules.analytics.dto.io;

public record DeviceSummaryIO(
        Integer organizationId,
        Long propertiesTotal,
        Long devicesPaired,
        Long devicesOnline,
        Long devicesLowBattery,
        Long propertiesWithoutDevice
) {
}

