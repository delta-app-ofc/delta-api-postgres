package br.com.delta.delta_api_postgres.modules.analytics.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface OrganizationMonthlyKpiProjection {
    Integer getOrganizationId();

    LocalDate getReferenceMonth();

    BigDecimal getTotalLiters();

    BigDecimal getTotalCost();

    BigDecimal getPreviousMonthLiters();

    BigDecimal getVariationPct();

    BigDecimal getProjectedLiters();

    BigDecimal getProjectedCost();

    Long getUnitsTotal();

    Long getUnitsOnTarget();

    Long getUnitsAttention();

    Long getUnitsCritical();

    BigDecimal getAvgGoalPct();

    BigDecimal getEstimatedSavingsValue();

    BigDecimal getSavedLiters();

    Long getMinDaysWithData();

    Boolean getIsCompleteMonth();
}

