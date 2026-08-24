package br.com.delta.delta_api_postgres.modules.device.repository;

import br.com.delta.delta_api_postgres.modules.device.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Integer> {
}
