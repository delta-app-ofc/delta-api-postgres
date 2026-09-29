package br.com.delta.delta_api_postgres.modules.analytics.dto.io;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlyVariationIO(
        Integer propertyId,
        String propertyName,
        LocalDate referenceMonth,
        BigDecimal totalLiters,
        BigDecimal previousMonthLiters,
        BigDecimal variationPct
) {
}
