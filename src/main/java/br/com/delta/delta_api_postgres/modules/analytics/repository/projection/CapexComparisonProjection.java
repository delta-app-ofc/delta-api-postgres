package br.com.delta.delta_api_postgres.modules.analytics.repository.projection;

import java.math.BigDecimal;

public interface CapexComparisonProjection {
    Integer getScenarioId();

    String getName();

    BigDecimal getInvestmentValue();

    BigDecimal getReductionPct();

    BigDecimal getAnnualSavingsValue();

    BigDecimal getPaybackMonths();

    Long getRankByPayback();
}
