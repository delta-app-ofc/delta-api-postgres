package br.com.delta.delta_api_postgres.modules.analytics.dto.io;

import java.math.BigDecimal;

public record InvestmentImpactIO(
        Integer scenarioId,
        Integer organizationId,
        String name,
        BigDecimal investmentValue,
        BigDecimal reductionPct,
        BigDecimal annualSavingsValue,
        BigDecimal paybackMonths,
        BigDecimal baselineAnnualLiters,
        BigDecimal savedLitersYear,
        BigDecimal tankTrucksEquivalent,
        BigDecimal familyMonthsEquivalent
) {
}

