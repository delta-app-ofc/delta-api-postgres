package br.com.delta.delta_api_postgres.modules.analytics.dto.io;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrganizationEsgIO(
        Integer organizationId,
        LocalDate referenceMonth,
        BigDecimal savedLitersMonth,
        BigDecimal savedLitersYearProjection,
        BigDecimal tankTrucksEquivalent,
        BigDecimal familyMonthsEquivalent,
        Long unitsTotal,
        Long unitsOnTarget,
        BigDecimal avgGoalPct,
        BigDecimal estimatedSavingsValue,
        Boolean isCompleteMonth
) {
}

