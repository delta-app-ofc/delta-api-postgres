package br.com.delta.delta_api_postgres.modules.analytics.dto.io;

import java.math.BigDecimal;

public record PropertyRankingIO(
        Integer propertyId,
        String propertyName,
        BigDecimal totalLiters,
        BigDecimal totalCost,
        BigDecimal litersPerM2,
        Long rankEfficiency,
        Long rankCost,
        Long consumptionQuartile,
        Double consumptionPercentRank
) {
}
