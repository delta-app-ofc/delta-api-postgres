package br.com.delta.delta_api_postgres.modules.region.repository;

import br.com.delta.delta_api_postgres.modules.region.entity.CityRegion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRegionRepository extends JpaRepository<CityRegion, Integer> {
    Optional<CityRegion> findByCityIgnoreCaseAndState(String city, String state);
}
