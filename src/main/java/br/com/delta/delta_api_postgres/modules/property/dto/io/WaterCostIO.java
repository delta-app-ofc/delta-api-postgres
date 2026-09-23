package br.com.delta.delta_api_postgres.modules.property.dto.io;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WaterCostIO(
        Integer propertyId,
        Integer regionId,
        BigDecimal consumptionM3,
        BigDecimal ratePerM3,
        BigDecimal estimatedCost,
        LocalDate referenceDate
) {
}
