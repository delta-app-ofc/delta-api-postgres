package br.com.delta.delta_api_postgres.modules.region_rate.mapper;

import br.com.delta.delta_api_postgres.modules.region_rate.dto.io.RegionRateIO;
import br.com.delta.delta_api_postgres.modules.region_rate.entity.RegionRate;
import org.springframework.stereotype.Component;

@Component
public class RegionRateMapper {

    public RegionRateIO toIO(RegionRate regionRate) {
        return new RegionRateIO(
                regionRate.getId(),
                regionRate.getRegion().getId(),
                regionRate.getM3Value(),
                regionRate.getInitialValidity(),
                regionRate.getFinalValidity()
        );
    }
}
