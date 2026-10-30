package br.com.delta.delta_api_postgres.modules.region.repository;

import br.com.delta.delta_api_postgres.AuthTestConfig;
import br.com.delta.delta_api_postgres.modules.region.entity.CityRegion;
import br.com.delta.delta_api_postgres.modules.region.entity.Region;
import br.com.delta.delta_api_postgres.modules.region.enums.RegionName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CityRegionRepositoryTests extends AuthTestConfig {
    @Autowired RegionRepository regions;
    @Autowired CityRegionRepository cityRegions;

    @Test void findsCityIgnoringCaseAndReturnsItsRegion() {
        var region = new Region();
        ReflectionTestUtils.setField(region, "name", RegionName.LINS);
        region = regions.saveAndFlush(region);
        cityRegions.saveAndFlush(new CityRegion("Lins", "SP", region));

        var found = cityRegions.findByCityIgnoreCaseAndState("LINS", "SP");

        assertThat(found).isPresent();
        assertThat(found.get().getRegion().getId()).isEqualTo(region.getId());
        assertThat(cityRegions.findByCityIgnoreCaseAndState("lins", "SP")).isPresent();
    }

    @Test void doesNotFindCityOutsideTheMapOrFromAnotherState() {
        assertThat(cityRegions.findByCityIgnoreCaseAndState("Campinas", "SP")).isEmpty();
        assertThat(cityRegions.findByCityIgnoreCaseAndState("Lins", "RJ")).isEmpty();
    }
}
