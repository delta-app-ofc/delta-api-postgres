package br.com.delta.delta_api_postgres.modules.habit.repository;

import br.com.delta.delta_api_postgres.modules.habit.entity.UserHabit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserHabitRepository
        extends JpaRepository<UserHabit, Integer> {

    List<UserHabit> findByUserId(Integer userId);

    boolean existsByUserIdAndHabit_Id(
            Integer userId,
            Integer habitId
    );
}
