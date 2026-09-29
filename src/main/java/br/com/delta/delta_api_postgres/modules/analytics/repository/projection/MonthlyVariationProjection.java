package br.com.delta.delta_api_postgres.modules.analytics.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MonthlyVariationProjection {
    Integer getPropertyId();

    String getPropertyName();

    LocalDate getReferenceMonth();

    BigDecimal getTotalLiters();

    BigDecimal getPreviousMonthLiters();

    BigDecimal getVariationPct();
}
