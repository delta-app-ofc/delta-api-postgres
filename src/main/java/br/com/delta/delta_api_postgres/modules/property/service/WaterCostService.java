package br.com.delta.delta_api_postgres.modules.property.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.property.dto.io.WaterCostIO;
import br.com.delta.delta_api_postgres.modules.property.repository.PropertyRepository;
import br.com.delta.delta_api_postgres.modules.property.repository.projection.WaterCostProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class WaterCostService {
    private final PropertyRepository propertyRepository;

    public WaterCostIO calculateWaterCost(Integer propertyId, BigDecimal consumptionM3, LocalDate referenceDate) {
        if(!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Propriedade nao " +
                    "encontrada");
        }


        WaterCostProjection waterCost = propertyRepository.calculateWaterCost(propertyId, consumptionM3, referenceDate);
        return new WaterCostIO(
                waterCost.getPropertyId(),
                waterCost.getRegionId(),
                waterCost.getConsumptionM3(),
                waterCost.getRatePerM3(),
                waterCost.getEstimatedCost(),
                waterCost.getReferenceDate()
        );
    }
}
