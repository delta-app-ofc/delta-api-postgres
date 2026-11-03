package br.com.delta.delta_api_postgres.modules.analytics.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PropertyMonthlyProjection {
    Integer getPropertyId();

    String getPropertyName();

    Integer getOrganizationId();

    String getCity();

    String getState();

    String getRegionName();

    BigDecimal getLatitude();

    BigDecimal getLongitude();

    BigDecimal getBuiltAreaM2();

    LocalDate getReferenceMonth();

    Long getDaysWithData();

    Boolean getIsCompleteMonth();

    BigDecimal getTotalLiters();

    BigDecimal getTotalCost();

    BigDecimal getLitersPerM2();

    BigDecimal getPreviousMonthLiters();

    BigDecimal getVariationPct();

    BigDecimal getMonthlyTargetLiters();

    BigDecimal getGoalPct();

    BigDecimal getDailyLimitLiters();

    BigDecimal getRemainingLiters();

    String getGoalStatus();

    BigDecimal getEstimatedSavingsValue();

    BigDecimal getSavedLiters();
}

