package br.com.delta.delta_api_postgres.modules.analytics.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface AlertProjection {
    Long getAlertKey();

    Integer getPropertyId();

    String getPropertyName();

    String getDeviceId();

    String getAlertType();

    String getSeverity();

    LocalDateTime getTriggeredAt();

    LocalDateTime getResolvedAt();

    BigDecimal getResolutionMinutes();
}

