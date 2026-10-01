package br.com.delta.delta_api_postgres.modules.analytics.repository.projection;

import java.math.BigDecimal;

public interface PropertyRankingProjection {
    Integer getPropertyId();

    String getPropertyName();

    BigDecimal getTotalLiters();

    BigDecimal getTotalCost();

    BigDecimal getLitersPerM2();

    Long getRankEfficiency();

    Long getRankCost();

    Long getConsumptionQuartile();

    Double getConsumptionPercentRank();
}
