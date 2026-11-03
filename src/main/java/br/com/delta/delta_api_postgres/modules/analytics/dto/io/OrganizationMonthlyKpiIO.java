package br.com.delta.delta_api_postgres.modules.analytics.dto.io;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrganizationMonthlyKpiIO(
        Integer organizationId,
        LocalDate referenceMonth,
        BigDecimal totalLiters,
        BigDecimal totalCost,
        BigDecimal previousMonthLiters,
        BigDecimal variationPct,
        BigDecimal projectedLiters,
        BigDecimal projectedCost,
        Long unitsTotal,
        Long unitsOnTarget,
        Long unitsAttention,
        Long unitsCritical,
        BigDecimal avgGoalPct,
        BigDecimal estimatedSavingsValue,
        BigDecimal savedLiters,
        Long minDaysWithData,
        Boolean isCompleteMonth
) {
}

