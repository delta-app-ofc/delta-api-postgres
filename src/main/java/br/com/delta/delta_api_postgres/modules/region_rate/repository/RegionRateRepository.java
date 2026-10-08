package br.com.delta.delta_api_postgres.modules.region_rate.repository;

import br.com.delta.delta_api_postgres.modules.region_rate.entity.RegionRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegionRateRepository extends JpaRepository<RegionRate, Integer> {

    List<RegionRate> findAllByRegionIdOrderByInitialValidityDesc(Integer regionId);

    List<RegionRate> findAllByRegionIdAndFinalValidityIsNullOrderByInitialValidityDesc(Integer regionId);
}
