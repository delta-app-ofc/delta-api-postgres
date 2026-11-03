package br.com.delta.delta_api_postgres.modules.analytics.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface MonitoringHourlyProjection {
    Integer getOrganizationId();

    Integer getPropertyId();

    String getPropertyName();

    Integer getWindowMinutes();

    LocalDateTime getReadingHour();

    BigDecimal getTotalLiters();

    BigDecimal getAvgFlowLmin();

    Long getReadingsCount();
}

