package br.com.delta.delta_api_postgres.modules.analytics.dto.io;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConsumptionDailyIO(
        Integer propertyId,
        String propertyName,
        LocalDate fullDate,
        BigDecimal totalLiters,
        BigDecimal costValue,
        BigDecimal runningTotalLiters,
        BigDecimal movingAvg7dLiters
) {
}
