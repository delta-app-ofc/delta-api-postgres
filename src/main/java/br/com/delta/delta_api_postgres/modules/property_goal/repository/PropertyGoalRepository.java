package br.com.delta.delta_api_postgres.modules.property_goal.repository;

import br.com.delta.delta_api_postgres.modules.property_goal.entity.PropertyGoal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyGoalRepository extends JpaRepository<PropertyGoal, Integer> {
}
