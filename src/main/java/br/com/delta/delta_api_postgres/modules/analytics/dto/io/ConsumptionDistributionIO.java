package br.com.delta.delta_api_postgres.modules.analytics.dto.io;

import java.math.BigDecimal;

public record ConsumptionDistributionIO(
        Integer propertyKey,
        BigDecimal totalLiters,
        Long distributionBucket,
        BigDecimal minLiters,
        Double q1Liters,
        Double medianLiters,
        Double q3Liters,
        BigDecimal maxLiters
) {
}
