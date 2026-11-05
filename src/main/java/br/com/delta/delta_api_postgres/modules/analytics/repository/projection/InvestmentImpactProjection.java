package br.com.delta.delta_api_postgres.modules.analytics.repository.projection;

import java.math.BigDecimal;

public interface InvestmentImpactProjection {
    Integer getScenarioId();

    Integer getOrganizationId();

    String getName();

    BigDecimal getInvestmentValue();

    BigDecimal getReductionPct();

    BigDecimal getAnnualSavingsValue();

    BigDecimal getPaybackMonths();

    BigDecimal getBaselineAnnualLiters();

    BigDecimal getSavedLitersYear();

    BigDecimal getTankTrucksEquivalent();

    BigDecimal getFamilyMonthsEquivalent();
}

