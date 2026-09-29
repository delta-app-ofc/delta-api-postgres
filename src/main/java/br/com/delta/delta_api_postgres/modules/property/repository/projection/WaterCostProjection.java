package br.com.delta.delta_api_postgres.modules.property.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface WaterCostProjection {
    Integer getPropertyId();

    Integer getRegionId();

    BigDecimal getConsumptionM3();

    BigDecimal getRatePerM3();

    BigDecimal getEstimatedCost();

    LocalDate getReferenceDate();
}
