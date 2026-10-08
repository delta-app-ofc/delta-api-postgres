package br.com.delta.delta_api_postgres.modules.region_rate.dto.io;

import br.com.delta.delta_api_postgres.modules.region.enums.RegionName;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegionRateIO(
        Integer id,
        RegionName regionName,
        BigDecimal m3Value,
        LocalDate initialValidity,
        LocalDate finalValidity
) {}
