package br.com.delta.delta_api_postgres.modules.habit.dto.requests;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record UpdateUserHabitRequest(

        @NotNull(message = "Habit ID é obrigatório")
        @Positive(message = "Habit ID deve ser positivo")
        Integer habitId,

        @NotNull(message = "Frequency é obrigatório")
        @Positive(message = "Frequência deve ser positiva")
        Integer frequency,

        LocalTime startTime,

        LocalTime endTime,

        @PositiveOrZero(message = "Estimated liters não pode ser negativo")
        @Digits(integer = 4, fraction = 2, message = "Estimated liters aceita até 4 dígitos inteiros e 2 decimais")
        BigDecimal estimatedLiters,

        @NotEmpty(message = "É necessário informar pelo menos um dia")
        List<@NotNull @Positive Integer> daysOfWeek

) {
}
