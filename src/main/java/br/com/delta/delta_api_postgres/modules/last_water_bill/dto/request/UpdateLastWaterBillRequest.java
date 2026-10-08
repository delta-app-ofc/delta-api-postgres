package br.com.delta.delta_api_postgres.modules.last_water_bill.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateLastWaterBillRequest(

        @NotNull(message = "User ID é obrigatório")
        @Positive(message = "User ID deve ser positivo")
        Integer userId,

        @NotNull(message = "Mês de referência é obrigatório")
        LocalDate month,

        @NotNull(message = "Valor total é obrigatório")
        @PositiveOrZero(message = "Valor total não pode ser negativo")
        BigDecimal totalValue,

        @NotNull(message = "Consumo em m³ é obrigatório")
        @PositiveOrZero(message = "Consumo em m³ não pode ser negativo")
        BigDecimal m3Value

) {
}
