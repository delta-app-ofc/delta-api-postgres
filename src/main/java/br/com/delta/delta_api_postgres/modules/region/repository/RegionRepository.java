package br.com.delta.delta_api_postgres.modules.region.repository;

import br.com.delta.delta_api_postgres.modules.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Integer> {
}
