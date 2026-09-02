package br.com.delta.delta_api_postgres.modules.habit.mapper;

import br.com.delta.delta_api_postgres.modules.habit.dto.io.UserHabitIO;
import br.com.delta.delta_api_postgres.modules.habit.dto.requests.CreateUserHabitRequest;
import br.com.delta.delta_api_postgres.modules.habit.dto.requests.UpdateUserHabitRequest;
import br.com.delta.delta_api_postgres.modules.habit.entity.UserHabit;
import org.springframework.stereotype.Component;

@Component
public class UserHabitMapper {

    public UserHabitIO fromCreateRequest(
            Integer userId,
            CreateUserHabitRequest request
    ) {
        return new UserHabitIO(
                null,
                userId,
                request.habitId(),
                null,
                null,
                request.frequency(),
                request.daysOfWeek()
        );
    }

    public UserHabitIO fromUpdateRequest(
            Integer id,
            Integer userId,
            UpdateUserHabitRequest request
    ) {
        return new UserHabitIO(
                id,
                userId,
                request.habitId(),
                null,
                null,
                request.frequency(),
                request.daysOfWeek()
        );
    }

    public UserHabitIO toIO(UserHabit userHabit) {

        return new UserHabitIO(
                userHabit.getId(),
                userHabit.getUserId(),
                userHabit.getHabit().getId(),
                userHabit.getHabit().getName(),
                userHabit.getHabit().getDescription(),
                userHabit.getFrequency(),
                userHabit.getDays()
                        .stream()
                        .map(day -> day.getDayOfWeek().getId())
                        .toList()
        );
    }
}
