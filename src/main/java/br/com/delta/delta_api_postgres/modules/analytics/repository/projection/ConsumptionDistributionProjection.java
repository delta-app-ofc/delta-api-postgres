package br.com.delta.delta_api_postgres.modules.analytics.repository.projection;

import java.math.BigDecimal;

public interface ConsumptionDistributionProjection {
    Integer getPropertyKey();

    BigDecimal getTotalLiters();

    Long getDistributionBucket();

    BigDecimal getMinLiters();

    Double getQ1Liters();

    Double getMedianLiters();

    Double getQ3Liters();

    BigDecimal getMaxLiters();
}
