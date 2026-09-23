package br.com.delta.delta_api_postgres.modules.region_rate.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.region.enums.RegionName;
import br.com.delta.delta_api_postgres.modules.region_rate.dto.io.RegionRateIO;
import br.com.delta.delta_api_postgres.modules.region_rate.entity.RegionRate;
import br.com.delta.delta_api_postgres.modules.region_rate.mapper.RegionRateMapper;
import br.com.delta.delta_api_postgres.modules.region_rate.repository.RegionRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegionRateServiceTest {

    private static final Integer REGION_ID = 1;

    @Mock
    private RegionRateRepository regionRateRepository;

    @Mock
    private RegionRateMapper regionRateMapper;

    @InjectMocks
    private RegionRateService regionRateService;

    @Test
    void findByRegion_quandoUltimaTarifaSolicitada_deveRetornarSomenteTarifasVigentes() {
        // Arrange
        RegionRate firstRate = new RegionRate();
        RegionRate secondRate = new RegionRate();
        RegionRateIO firstIO = regionRateIO(
                1,
                new BigDecimal("8.50"),
                LocalDate.of(2026, 1, 1),
                null
        );
        RegionRateIO secondIO = regionRateIO(
                2,
                new BigDecimal("7.90"),
                LocalDate.of(2025, 1, 1),
                null
        );

        when(regionRateRepository
                .findAllByRegionIdAndFinalValidityIsNullOrderByInitialValidityDesc(REGION_ID))
                .thenReturn(List.of(firstRate, secondRate));
        when(regionRateMapper.toIO(firstRate)).thenReturn(firstIO);
        when(regionRateMapper.toIO(secondRate)).thenReturn(secondIO);

        // Act
        List<RegionRateIO> result = regionRateService.findByRegion(REGION_ID, true);

        // Assert
        assertThat(result).containsExactly(firstIO, secondIO);
        verify(regionRateRepository, never())
                .findAllByRegionIdOrderByInitialValidityDesc(REGION_ID);
        verify(regionRateMapper).toIO(firstRate);
        verify(regionRateMapper).toIO(secondRate);
    }

    @Test
    void findByRegion_quandoTodasAsTarifasSolicitadas_deveRetornarHistoricoCompleto() {
        RegionRate currentRate = new RegionRate();
        RegionRate previousRate = new RegionRate();
        RegionRateIO currentIO = regionRateIO(
                1,
                new BigDecimal("8.50"),
                LocalDate.of(2026, 1, 1),
                null
        );
        RegionRateIO previousIO = regionRateIO(
                2,
                new BigDecimal("7.50"),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31)
        );

        when(regionRateRepository.findAllByRegionIdOrderByInitialValidityDesc(REGION_ID))
                .thenReturn(List.of(currentRate, previousRate));
        when(regionRateMapper.toIO(currentRate)).thenReturn(currentIO);
        when(regionRateMapper.toIO(previousRate)).thenReturn(previousIO);

        List<RegionRateIO> result = regionRateService.findByRegion(REGION_ID, false);

        assertThat(result).containsExactly(currentIO, previousIO);
        verify(regionRateRepository, never())
                .findAllByRegionIdAndFinalValidityIsNullOrderByInitialValidityDesc(REGION_ID);
    }

    @Test
    void findByRegion_quandoParametroLastForNulo_deveRetornarHistoricoCompleto() {
        RegionRate rate = new RegionRate();
        RegionRateIO expected = regionRateIO(
                1,
                new BigDecimal("8.50"),
                LocalDate.of(2026, 1, 1),
                null
        );

        when(regionRateRepository.findAllByRegionIdOrderByInitialValidityDesc(REGION_ID))
                .thenReturn(List.of(rate));
        when(regionRateMapper.toIO(rate)).thenReturn(expected);

        List<RegionRateIO> result = regionRateService.findByRegion(REGION_ID, null);

        assertThat(result).containsExactly(expected);
        verify(regionRateRepository, never())
                .findAllByRegionIdAndFinalValidityIsNullOrderByInitialValidityDesc(REGION_ID);
    }

    @Test
    void findByRegion_quandoNaoExisteTarifaVigente_deveLancarResourceNotFoundException() {
        when(regionRateRepository
                .findAllByRegionIdAndFinalValidityIsNullOrderByInitialValidityDesc(REGION_ID))
                .thenReturn(List.of());

        assertThatThrownBy(() -> regionRateService.findByRegion(REGION_ID, true))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tarifa vigente não encontrada para a região");

        verifyNoInteractions(regionRateMapper);
        verify(regionRateRepository, never())
                .findAllByRegionIdOrderByInitialValidityDesc(REGION_ID);
    }

    @Test
    void findByRegion_quandoNaoExisteHistorico_deveLancarResourceNotFoundException() {
        when(regionRateRepository.findAllByRegionIdOrderByInitialValidityDesc(REGION_ID))
                .thenReturn(List.of());

        assertThatThrownBy(() -> regionRateService.findByRegion(REGION_ID, false))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Nenhuma tarifa encontrada para a região");

        verifyNoInteractions(regionRateMapper);
        verify(regionRateRepository, never())
                .findAllByRegionIdAndFinalValidityIsNullOrderByInitialValidityDesc(REGION_ID);
    }

    @Test
    void findByRegion_quandoLastForNuloENaoExisteHistorico_deveLancarResourceNotFoundException() {
        when(regionRateRepository.findAllByRegionIdOrderByInitialValidityDesc(REGION_ID))
                .thenReturn(List.of());

        assertThatThrownBy(() -> regionRateService.findByRegion(REGION_ID, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Nenhuma tarifa encontrada para a região");

        verifyNoInteractions(regionRateMapper);
    }

    private RegionRateIO regionRateIO(
            Integer id,
            BigDecimal m3Value,
            LocalDate initialValidity,
            LocalDate finalValidity
    ) {
        return new RegionRateIO(
                id,
                RegionName.SUL,
                m3Value,
                initialValidity,
                finalValidity
        );
    }
}
