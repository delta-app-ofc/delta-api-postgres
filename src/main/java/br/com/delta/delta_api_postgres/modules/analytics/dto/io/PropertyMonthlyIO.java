package br.com.delta.delta_api_postgres.modules.analytics.dto.io;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PropertyMonthlyIO(
        Integer propertyId,
        String propertyName,
        Integer organizationId,
        String city,
        String state,
        String regionName,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal builtAreaM2,
        LocalDate referenceMonth,
        Long daysWithData,
        Boolean isCompleteMonth,
        BigDecimal totalLiters,
        BigDecimal totalCost,
        BigDecimal litersPerM2,
        BigDecimal previousMonthLiters,
        BigDecimal variationPct,
        BigDecimal monthlyTargetLiters,
        BigDecimal goalPct,
        BigDecimal dailyLimitLiters,
        BigDecimal remainingLiters,
        String goalStatus,
        BigDecimal estimatedSavingsValue,
        BigDecimal savedLiters
) {
}

