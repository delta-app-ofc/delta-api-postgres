package br.com.delta.delta_api_postgres.modules.region_rate.dto.io;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegionRateIO(
        Integer id,
        Integer regionId,
        BigDecimal m3Value,
        LocalDate initialValidity,
        LocalDate finalValidity
) {}
