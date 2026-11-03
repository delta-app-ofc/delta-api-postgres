package br.com.delta.delta_api_postgres.modules.analytics.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface LeakProjection {
    Long getAlertKey();

    Integer getOrganizationId();

    Integer getPropertyId();

    String getPropertyName();

    String getDeviceId();

    String getSeverity();

    LocalDateTime getTriggeredAt();

    LocalDateTime getResolvedAt();

    String getLeakStatus();

    BigDecimal getDurationMinutes();

    BigDecimal getEstimatedLossLiters();

    BigDecimal getEstimatedLossCost();
}

