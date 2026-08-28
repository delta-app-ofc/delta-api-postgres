package br.com.delta.delta_api_postgres.modules.property.repository;

import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Integer> {
}
