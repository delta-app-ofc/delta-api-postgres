package br.com.delta.delta_api_postgres.modules.habit.repository;

import br.com.delta.delta_api_postgres.modules.habit.entity.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DayOfWeekRepository
        extends JpaRepository<DayOfWeek, Integer> {
}