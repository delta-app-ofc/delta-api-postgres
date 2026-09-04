package br.com.delta.delta_api_postgres.modules.habit.dto.io;

import java.util.List;

public record UserHabitIO(
        Integer id,
        Integer userId,
        Integer habitId,
        String habitName,
        String habitDescription,
        Integer frequency,
        List<Integer> daysOfWeek
) {
}
