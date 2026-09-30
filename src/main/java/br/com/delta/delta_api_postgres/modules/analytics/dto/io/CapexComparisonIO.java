package br.com.delta.delta_api_postgres.modules.analytics.dto.io;

import java.math.BigDecimal;

public record CapexComparisonIO(
        Integer scenarioId,
        String name,
        BigDecimal investmentValue,
        BigDecimal reductionPct,
        BigDecimal annualSavingsValue,
        BigDecimal paybackMonths,
        Long rankByPayback
) {
}
