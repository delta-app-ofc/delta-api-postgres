package br.com.delta.delta_api_postgres.repository;

import br.com.delta.delta_api_postgres.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Integer> {
}
