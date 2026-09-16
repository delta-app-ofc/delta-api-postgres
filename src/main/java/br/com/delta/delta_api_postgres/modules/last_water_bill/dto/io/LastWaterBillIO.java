package br.com.delta.delta_api_postgres.modules.last_water_bill.dto.io;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LastWaterBillIO(
        Integer id,
        Integer userId,
        LocalDate month,
        BigDecimal totalValue,
        BigDecimal m3Value
) {
}
