package br.com.delta.delta_api_postgres.modules.habit.repository;

import br.com.delta.delta_api_postgres.modules.habit.entity.UserHabitDay;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHabitDayRepository
        extends JpaRepository<UserHabitDay, Integer> {
}