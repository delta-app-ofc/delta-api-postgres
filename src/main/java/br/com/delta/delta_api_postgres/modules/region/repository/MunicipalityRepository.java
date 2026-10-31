package br.com.delta.delta_api_postgres.modules.region.repository;

import br.com.delta.delta_api_postgres.modules.region.entity.Municipality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MunicipalityRepository extends JpaRepository<Municipality, Integer> {
    Optional<Municipality> findByNameIgnoreCase(String name);
}
