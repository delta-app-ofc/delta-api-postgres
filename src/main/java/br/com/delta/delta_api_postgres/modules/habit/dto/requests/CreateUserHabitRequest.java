package br.com.delta.delta_api_postgres.modules.habit.dto.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record CreateUserHabitRequest(

        @NotNull(message = "Habit ID é obrigatório")
        Integer habitId,

        @NotNull(message = "Frequency é obrigatório")
        Integer frequency,

        LocalTime startTime,

        LocalTime endTime,

        BigDecimal estimatedLiters,

        @NotEmpty(message = "É necessário informar pelo menos um dia")
        List<Integer> daysOfWeek

) {
}