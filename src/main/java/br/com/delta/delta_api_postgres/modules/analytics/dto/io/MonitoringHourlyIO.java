package br.com.delta.delta_api_postgres.modules.analytics.dto.io;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MonitoringHourlyIO(
        Integer organizationId,
        Integer propertyId,
        String propertyName,
        Integer windowMinutes,
        LocalDateTime readingHour,
        BigDecimal totalLiters,
        BigDecimal avgFlowLmin,
        Long readingsCount
) {
}

