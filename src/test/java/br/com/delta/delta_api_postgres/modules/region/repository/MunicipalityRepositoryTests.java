package br.com.delta.delta_api_postgres.modules.region.repository;

import br.com.delta.delta_api_postgres.AuthTestConfig;
import br.com.delta.delta_api_postgres.modules.region.entity.Municipality;
import br.com.delta.delta_api_postgres.modules.region.entity.Region;
import br.com.delta.delta_api_postgres.modules.region.enums.RegionName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MunicipalityRepositoryTests extends AuthTestConfig {
    @Autowired RegionRepository regions;
    @Autowired MunicipalityRepository municipalities;

    @Test void findsCityIgnoringCaseAndReturnsItsRegion() {
        var region = new Region();
        ReflectionTestUtils.setField(region, "name", RegionName.LINS);
        region = regions.saveAndFlush(region);
        municipalities.saveAndFlush(new Municipality("Lins", 3527108, region));

        var found = municipalities.findByNameIgnoreCase("LINS");

        assertThat(found).isPresent();
        assertThat(found.get().getRegion().getId()).isEqualTo(region.getId());
        assertThat(municipalities.findByNameIgnoreCase("lins")).isPresent();
    }

    @Test void doesNotFindCityOutsideTheList() {
        assertThat(municipalities.findByNameIgnoreCase("Campinas")).isEmpty();
    }
}
