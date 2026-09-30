package br.com.delta.delta_api_postgres.modules.analytics.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ResidentialEfficiencyRankingProjection {
    Integer getUserId();

    String getPersonName();

    LocalDate getReferenceMonth();

    BigDecimal getM3Value();

    BigDecimal getTotalValue();

    Long getRankEfficiency();

    Double getConsumptionPercentRank();
}
