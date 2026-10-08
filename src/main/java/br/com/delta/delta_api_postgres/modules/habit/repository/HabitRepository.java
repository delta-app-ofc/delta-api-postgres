package br.com.delta.delta_api_postgres.modules.habit.repository;

import br.com.delta.delta_api_postgres.modules.habit.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitRepository extends JpaRepository<Habit, Integer> {
}