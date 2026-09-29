package br.com.delta.delta_api_postgres.modules.organization.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceAlreadyExistsException;
import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.organization.dto.io.OrganizationIO;
import br.com.delta.delta_api_postgres.modules.organization.entity.Organization;
import br.com.delta.delta_api_postgres.modules.organization.enums.OrganizationSegment;
import br.com.delta.delta_api_postgres.modules.organization.mapper.OrganizationMapper;
import br.com.delta.delta_api_postgres.modules.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    private static final Integer ORGANIZATION_ID = 1;
    private static final String CORPORATE_NAME = "Empresa Teste LTDA";
    private static final String TRADE_NAME = "Empresa Teste";
    private static final String CNPJ = "12345678000199";
    private static final OrganizationSegment SEGMENT = OrganizationSegment.INDUSTRIA;
    private static final Integer DECLARED_UNIT_COUNT = 5;
    private static final LocalDate REGISTRATION_DATE = LocalDate.of(2026, 1, 15);

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMapper organizationMapper;

    @InjectMocks
    private OrganizationService organizationService;

    @Test
    void create_quandoDadosValidos_deveSalvarERetornarOrganizacao() {
        OrganizationIO input = inputOrganizationIO();
        Organization organization = organization(null);
        Organization savedOrganization = organization(ORGANIZATION_ID);
        OrganizationIO expected = savedOrganizationIO();

        when(organizationRepository.existsByCnpj(CNPJ)).thenReturn(false);
        when(organizationMapper.toEntity(input)).thenReturn(organization);
        when(organizationRepository.save(organization)).thenReturn(savedOrganization);
        when(organizationMapper.toIO(savedOrganization)).thenReturn(expected);

        OrganizationIO result = organizationService.create(input);

        assertThat(result).isEqualTo(expected);
        verify(organizationRepository).save(organization);
        verify(organizationMapper).toIO(savedOrganization);
    }

    @Test
    void create_quandoCnpjJaExiste_deveLancarResourceAlreadyExistsException() {
        OrganizationIO input = inputOrganizationIO();
        when(organizationRepository.existsByCnpj(CNPJ)).thenReturn(true);

        assertThatThrownBy(() -> organizationService.create(input))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Já existe uma organização cadastrada com esse CNPJ");

        verify(organizationRepository, never()).save(any());
        verifyNoInteractions(organizationMapper);
    }

    @Test
    void findAll_quandoExistemOrganizacoes_deveRetornarTodasMapeadas() {
        Organization firstOrganization = organization(ORGANIZATION_ID);
        Organization secondOrganization = organization(2);
        OrganizationIO firstIO = savedOrganizationIO();
        OrganizationIO secondIO = new OrganizationIO(
                2,
                CORPORATE_NAME,
                TRADE_NAME,
                "98765432000188",
                SEGMENT,
                DECLARED_UNIT_COUNT,
                REGISTRATION_DATE
        );

        when(organizationRepository.findAll()).thenReturn(List.of(firstOrganization, secondOrganization));
        when(organizationMapper.toIO(firstOrganization)).thenReturn(firstIO);
        when(organizationMapper.toIO(secondOrganization)).thenReturn(secondIO);

        List<OrganizationIO> result = organizationService.findAll();

        assertThat(result).containsExactly(firstIO, secondIO);
        verify(organizationMapper).toIO(firstOrganization);
        verify(organizationMapper).toIO(secondOrganization);
    }

    @Test
    void findAll_quandoNaoExistemOrganizacoes_deveRetornarListaVazia() {
        when(organizationRepository.findAll()).thenReturn(List.of());

        List<OrganizationIO> result = organizationService.findAll();

        assertThat(result).isEmpty();
        verifyNoInteractions(organizationMapper);
    }

    @Test
    void findById_quandoOrganizacaoExiste_deveRetornarOrganizacaoMapeada() {
        Organization organization = organization(ORGANIZATION_ID);
        OrganizationIO expected = savedOrganizationIO();

        when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(organization));
        when(organizationMapper.toIO(organization)).thenReturn(expected);

        OrganizationIO result = organizationService.findById(ORGANIZATION_ID);

        assertThat(result).isEqualTo(expected);
        verify(organizationMapper).toIO(organization);
    }

    @Test
    void findById_quandoOrganizacaoNaoExiste_deveLancarResourceNotFoundException() {
        when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationService.findById(ORGANIZATION_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Organização não encontrada");

        verifyNoInteractions(organizationMapper);
    }

    @Test
    void update_quandoDadosValidos_deveAtualizarERetornarOrganizacao() {
        OrganizationIO input = inputOrganizationIO();
        Organization organization = organization(ORGANIZATION_ID);
        OrganizationIO expected = savedOrganizationIO();

        when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(organization));
        when(organizationRepository.save(organization)).thenReturn(organization);
        when(organizationMapper.toIO(organization)).thenReturn(expected);

        OrganizationIO result = organizationService.update(ORGANIZATION_ID, input);

        assertThat(result).isEqualTo(expected);
        verify(organizationMapper).updateEntity(organization, input);
        verify(organizationRepository).save(organization);
    }

    @Test
    void update_quandoCnpjMudaParaUmJaExistente_deveLancarResourceAlreadyExistsException() {
        OrganizationIO input = new OrganizationIO(
                ORGANIZATION_ID,
                CORPORATE_NAME,
                TRADE_NAME,
                "98765432000188",
                SEGMENT,
                DECLARED_UNIT_COUNT,
                null
        );
        Organization organization = organization(ORGANIZATION_ID);

        when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(organization));
        when(organizationRepository.existsByCnpj("98765432000188")).thenReturn(true);

        assertThatThrownBy(() -> organizationService.update(ORGANIZATION_ID, input))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Já existe uma organização cadastrada com esse CNPJ");

        verify(organizationRepository, never()).save(any());
        verifyNoInteractions(organizationMapper);
    }

    @Test
    void update_quandoOrganizacaoNaoExiste_deveLancarResourceNotFoundException() {
        OrganizationIO input = inputOrganizationIO();
        when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationService.update(ORGANIZATION_ID, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Organização não encontrada");

        verify(organizationRepository, never()).save(any());
        verifyNoInteractions(organizationMapper);
    }

    @Test
    void delete_quandoOrganizacaoExiste_deveExcluirOrganizacao() {
        Organization organization = organization(ORGANIZATION_ID);
        when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(organization));

        organizationService.delete(ORGANIZATION_ID);

        verify(organizationRepository).delete(organization);
    }

    @Test
    void delete_quandoOrganizacaoNaoExiste_deveLancarResourceNotFoundException() {
        when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationService.delete(ORGANIZATION_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Organização não encontrada");

        verify(organizationRepository, never()).delete(any());
    }

    private OrganizationIO inputOrganizationIO() {
        return new OrganizationIO(
                null,
                CORPORATE_NAME,
                TRADE_NAME,
                CNPJ,
                SEGMENT,
                DECLARED_UNIT_COUNT,
                null
        );
    }

    private OrganizationIO savedOrganizationIO() {
        return new OrganizationIO(
                ORGANIZATION_ID,
                CORPORATE_NAME,
                TRADE_NAME,
                CNPJ,
                SEGMENT,
                DECLARED_UNIT_COUNT,
                REGISTRATION_DATE
        );
    }

    private Organization organization(Integer id) {
        return new Organization(
                id,
                CORPORATE_NAME,
                TRADE_NAME,
                CNPJ,
                SEGMENT,
                DECLARED_UNIT_COUNT,
                REGISTRATION_DATE
        );
    }
}
