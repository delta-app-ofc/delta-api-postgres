package br.com.delta.delta_api_postgres.modules.analytics.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ConsumptionDailyProjection {
    Integer getPropertyId();

    String getPropertyName();

    LocalDate getFullDate();

    BigDecimal getTotalLiters();

    BigDecimal getCostValue();

    BigDecimal getRunningTotalLiters();

    BigDecimal getMovingAvg7dLiters();
}
