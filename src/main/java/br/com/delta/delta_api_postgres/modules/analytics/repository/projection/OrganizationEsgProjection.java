package br.com.delta.delta_api_postgres.modules.analytics.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface OrganizationEsgProjection {
    Integer getOrganizationId();

    LocalDate getReferenceMonth();

    BigDecimal getSavedLitersMonth();

    BigDecimal getSavedLitersYearProjection();

    BigDecimal getTankTrucksEquivalent();

    BigDecimal getFamilyMonthsEquivalent();

    Long getUnitsTotal();

    Long getUnitsOnTarget();

    BigDecimal getAvgGoalPct();

    BigDecimal getEstimatedSavingsValue();
}

