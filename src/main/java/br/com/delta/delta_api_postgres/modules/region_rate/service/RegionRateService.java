package br.com.delta.delta_api_postgres.modules.region_rate.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.region_rate.dto.io.RegionRateIO;
import br.com.delta.delta_api_postgres.modules.region_rate.entity.RegionRate;
import br.com.delta.delta_api_postgres.modules.region_rate.mapper.RegionRateMapper;
import br.com.delta.delta_api_postgres.modules.region_rate.repository.RegionRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionRateService {

    private final RegionRateRepository regionRateRepository;
    private final RegionRateMapper regionRateMapper;

    @Transactional(readOnly = true)
    public List<RegionRateIO> findByRegion(Integer regionId, Boolean last) {
        boolean lastWasRequested = Boolean.TRUE.equals(last);

        List<RegionRate> regionRates = lastWasRequested
                ? regionRateRepository.findAllByRegionIdAndFinalValidityIsNullOrderByInitialValidityDesc(regionId)
                : regionRateRepository.findAllByRegionIdOrderByInitialValidityDesc(regionId);

        if (regionRates.isEmpty()) {
            throw new ResourceNotFoundException(
                    lastWasRequested
                            ? "Tarifa vigente não encontrada para a região"
                            : "Nenhuma tarifa encontrada para a região"
            );
        }

        return regionRates.stream().map(regionRateMapper::toIO).toList();
    }
}
