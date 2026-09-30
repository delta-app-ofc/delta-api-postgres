package br.com.delta.delta_api_postgres.modules.analytics.dto.io;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResidentialEfficiencyRankingIO(
        Integer userId,
        String personName,
        LocalDate referenceMonth,
        BigDecimal m3Value,
        BigDecimal totalValue,
        Long rankEfficiency,
        Double consumptionPercentRank
) {
}
