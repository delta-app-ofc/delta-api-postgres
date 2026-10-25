package br.com.delta.delta_api_postgres.modules.habit.dto.io;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record UserHabitIO(
        Integer id,
        Integer userId,
        Integer habitId,
        String habitName,
        String habitCategory,
        String habitDescription,
        Integer frequency,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal estimatedLiters,
        List<Integer> daysOfWeek
) {
}
