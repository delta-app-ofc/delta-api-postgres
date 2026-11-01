package br.com.delta.delta_api_postgres.modules.property.service;

import br.com.delta.delta_api_postgres.modules.auth.security.UserDataAccess;
import br.com.delta.delta_api_postgres.modules.user_property.repository.UserPropertyRepository;
import br.com.delta.delta_api_postgres.modules.device.repository.DeviceRepository;

import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.address.entity.Address;
import br.com.delta.delta_api_postgres.modules.address.repository.AddressRepository;
import br.com.delta.delta_api_postgres.modules.organization.entity.Organization;
import br.com.delta.delta_api_postgres.modules.organization.repository.OrganizationRepository;
import br.com.delta.delta_api_postgres.modules.property.dto.io.PropertyIO;
import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import br.com.delta.delta_api_postgres.modules.property.entity.PropertyClassificationEntity;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyClassification;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyClassificationGroup;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyType;
import br.com.delta.delta_api_postgres.modules.property.mapper.PropertyMapper;
import br.com.delta.delta_api_postgres.modules.property.repository.PropertyClassificationRepository;
import br.com.delta.delta_api_postgres.modules.property.repository.PropertyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
class PropertyServiceTest {
    @Mock private UserPropertyRepository userPropertyRepository;
    @Mock private DeviceRepository deviceRepository;
    @Mock private UserDataAccess access;

    private static final Integer PROPERTY_ID = 1;
    private static final Integer ADDRESS_ID = 10;
    private static final Integer USER_ID = 5;
    private static final Integer ORGANIZATION_ID = 20;
    private static final String PROPERTY_NAME = "Casa principal";
    private static final PropertyType PROPERTY_TYPE = PropertyType.CASA;
    private static final PropertyClassification CLASSIFICATION = PropertyClassification.RESIDENCIAL_NORMAL;
    private static final BigDecimal BUILT_AREA_M2 = new BigDecimal("120.50");
    private static final LocalDate REGISTRATION_DATE = LocalDate.of(2026, 1, 15);

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private PropertyClassificationRepository propertyClassificationRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private PropertyMapper propertyMapper;

    @InjectMocks
    private PropertyService propertyService;

    @Test
    void create_whenValidData_callsProcedureAndReturnsProperty() {
        PropertyIO input = inputPropertyIO();
        Property savedProperty = property(PROPERTY_ID, PROPERTY_NAME, ORGANIZATION_ID, BUILT_AREA_M2);
        PropertyIO expected = savedPropertyIO();

        when(propertyRepository.registerProperty(
                USER_ID, PROPERTY_NAME, PROPERTY_TYPE.name(), CLASSIFICATION.name(),
                ADDRESS_ID, ORGANIZATION_ID, BUILT_AREA_M2
        )).thenReturn(PROPERTY_ID);
        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(savedProperty));
        when(propertyMapper.toIO(savedProperty)).thenReturn(expected);

        PropertyIO result = propertyService.create(USER_ID, input);

        assertThat(result).isEqualTo(expected);
        verify(propertyMapper).toIO(savedProperty);
        verify(userPropertyRepository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(link ->
                link.getUserId().equals(USER_ID) && link.getProperty() == savedProperty));
    }

    @Test
    void create_whenProcedureAlreadyCreatesLink_doesNotDuplicateAssociation() {
        Property savedProperty = property(PROPERTY_ID, PROPERTY_NAME, ORGANIZATION_ID, BUILT_AREA_M2);
        when(propertyRepository.registerProperty(USER_ID, PROPERTY_NAME, PROPERTY_TYPE.name(), CLASSIFICATION.name(),
                ADDRESS_ID, ORGANIZATION_ID, BUILT_AREA_M2)).thenReturn(PROPERTY_ID);
        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(savedProperty));
        when(userPropertyRepository.existsByUserIdAndProperty_Id(USER_ID, PROPERTY_ID)).thenReturn(true);

        propertyService.create(USER_ID, inputPropertyIO());

        verify(userPropertyRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_whenProcedureDoesNotReturnValidProperty_throwsIllegalStateException() {
        PropertyIO input = inputPropertyIO();

        when(propertyRepository.registerProperty(
                USER_ID, PROPERTY_NAME, PROPERTY_TYPE.name(), CLASSIFICATION.name(),
                ADDRESS_ID, ORGANIZATION_ID, BUILT_AREA_M2
        )).thenReturn(PROPERTY_ID);
        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.create(USER_ID, input))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A procedure não retornou uma propriedade válida");

        verifyNoInteractions(propertyMapper);
    }

    @Test
    void findAll_whenPropertiesExist_returnsAllMapped() {
        when(access.userId()).thenReturn(1);
        Property firstProperty = property(PROPERTY_ID, PROPERTY_NAME, ORGANIZATION_ID, BUILT_AREA_M2);
        Property secondProperty = property(2, "Loja", null, null);
        PropertyIO firstIO = savedPropertyIO();
        PropertyIO secondIO = new PropertyIO(
                2,
                "Loja",
                PropertyType.CASA,
                CLASSIFICATION,
                ADDRESS_ID,
                null,
                null,
                REGISTRATION_DATE
        );

        when(propertyRepository.findForUser(1)).thenReturn(List.of(firstProperty, secondProperty));
        when(propertyMapper.toIO(firstProperty)).thenReturn(firstIO);
        when(propertyMapper.toIO(secondProperty)).thenReturn(secondIO);

        List<PropertyIO> result = propertyService.findAll();

        assertThat(result).containsExactly(firstIO, secondIO);
        verify(propertyMapper).toIO(firstProperty);
        verify(propertyMapper).toIO(secondProperty);
    }

    @Test
    void findAll_whenNoPropertiesExist_returnsEmptyList() {
        when(access.userId()).thenReturn(1);
        when(propertyRepository.findForUser(1)).thenReturn(List.of());

        List<PropertyIO> result = propertyService.findAll();

        assertThat(result).isEmpty();
        verifyNoInteractions(propertyMapper);
    }

    @Test
    void findById_whenPropertyExists_returnsPropertyMapped() {
        Property property = property(PROPERTY_ID, PROPERTY_NAME, ORGANIZATION_ID, BUILT_AREA_M2);
        PropertyIO expected = savedPropertyIO();

        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property));
        when(propertyMapper.toIO(property)).thenReturn(expected);

        PropertyIO result = propertyService.findById(PROPERTY_ID);

        assertThat(result).isEqualTo(expected);
        verify(propertyMapper).toIO(property);
    }

    @Test
    void findById_whenPropertyDoesNotExist_throwsResourceNotFoundException() {
        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.findById(PROPERTY_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Propriedade nao encontrada");

        verifyNoInteractions(propertyMapper);
    }

    @Test
    void update_whenValidData_updatesAndReturnsProperty() {
        PropertyIO input = inputPropertyIO();
        Address address = address();
        PropertyClassificationEntity classification = classificationEntity();
        Organization organization = organization();
        Property property = property(PROPERTY_ID, "Nome antigo", null, null);
        Property updatedProperty = property(PROPERTY_ID, PROPERTY_NAME, ORGANIZATION_ID, BUILT_AREA_M2);
        PropertyIO expected = savedPropertyIO();

        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property));
        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.of(address));
        when(propertyClassificationRepository.findByName(CLASSIFICATION)).thenReturn(Optional.of(classification));
        when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(organization));
        when(propertyRepository.save(property)).thenReturn(updatedProperty);
        when(propertyMapper.toIO(updatedProperty)).thenReturn(expected);

        PropertyIO result = propertyService.update(PROPERTY_ID, input);

        assertThat(result).isEqualTo(expected);
        verify(propertyMapper).updateEntity(property, input, address, classification, organization);
        verify(propertyRepository).save(property);
    }

    @Test
    void update_whenWithoutOrganization_doesNotQueryOrganizationRepository() {
        PropertyIO input = inputPropertyIOSemOrganizacao();
        Address address = address();
        PropertyClassificationEntity classification = classificationEntity();
        Property property = property(PROPERTY_ID, "Nome antigo", null, null);
        Property updatedProperty = property(PROPERTY_ID, PROPERTY_NAME, null, null);
        PropertyIO expected = savedPropertyIOSemOrganizacao();

        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property));
        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.of(address));
        when(propertyClassificationRepository.findByName(CLASSIFICATION)).thenReturn(Optional.of(classification));
        when(propertyRepository.save(property)).thenReturn(updatedProperty);
        when(propertyMapper.toIO(updatedProperty)).thenReturn(expected);

        PropertyIO result = propertyService.update(PROPERTY_ID, input);

        assertThat(result).isEqualTo(expected);
        verify(propertyMapper).updateEntity(property, input, address, classification, null);
        verifyNoInteractions(organizationRepository);
    }

    @Test
    void update_whenPropertyDoesNotExist_throwsResourceNotFoundException() {
        PropertyIO input = inputPropertyIO();
        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.update(PROPERTY_ID, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Propriedade nao encontrada");

        verify(propertyRepository, never()).save(any());
        verifyNoInteractions(addressRepository, propertyClassificationRepository, organizationRepository, propertyMapper);
    }

    @Test
    void update_whenAddressDoesNotExist_throwsResourceNotFoundException() {
        PropertyIO input = inputPropertyIO();
        Property property = property(PROPERTY_ID, PROPERTY_NAME, null, null);

        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property));
        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.update(PROPERTY_ID, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Endereço nao encontrado");

        verify(propertyRepository, never()).save(any());
        verifyNoInteractions(propertyClassificationRepository, organizationRepository, propertyMapper);
    }

    @Test
    void update_whenClassificationDoesNotExist_throwsResourceNotFoundException() {
        PropertyIO input = inputPropertyIO();
        Property property = property(PROPERTY_ID, PROPERTY_NAME, null, null);

        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property));
        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.of(address()));
        when(propertyClassificationRepository.findByName(CLASSIFICATION)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.update(PROPERTY_ID, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Classificação não encontrada");

        verify(propertyRepository, never()).save(any());
        verifyNoInteractions(organizationRepository, propertyMapper);
    }

    @Test
    void update_whenOrganizationDoesNotExist_throwsResourceNotFoundException() {
        PropertyIO input = inputPropertyIO();
        Property property = property(PROPERTY_ID, PROPERTY_NAME, null, null);

        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property));
        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.of(address()));
        when(propertyClassificationRepository.findByName(CLASSIFICATION)).thenReturn(Optional.of(classificationEntity()));
        when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.update(PROPERTY_ID, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Organização não encontrada");

        verify(propertyRepository, never()).save(any());
        verifyNoInteractions(propertyMapper);
    }

    @Test
    void delete_whenPropertyExists_deletesProperty() {
        Property property = property(PROPERTY_ID, PROPERTY_NAME, null, null);
        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property));

        propertyService.delete(PROPERTY_ID);

        verify(propertyRepository).delete(property);
    }

    @Test
    void delete_whenPropertyDoesNotExist_throwsResourceNotFoundException() {
        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.delete(PROPERTY_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Propriedade nao encontrada");

        verify(propertyRepository, never()).delete(any());
    }

    private PropertyIO inputPropertyIO() {
        return new PropertyIO(
                null,
                PROPERTY_NAME,
                PROPERTY_TYPE,
                CLASSIFICATION,
                ADDRESS_ID,
                ORGANIZATION_ID,
                BUILT_AREA_M2,
                null
        );
    }

    private PropertyIO inputPropertyIOSemOrganizacao() {
        return new PropertyIO(
                null,
                PROPERTY_NAME,
                PROPERTY_TYPE,
                CLASSIFICATION,
                ADDRESS_ID,
                null,
                null,
                null
        );
    }

    private PropertyIO savedPropertyIO() {
        return new PropertyIO(
                PROPERTY_ID,
                PROPERTY_NAME,
                PROPERTY_TYPE,
                CLASSIFICATION,
                ADDRESS_ID,
                ORGANIZATION_ID,
                BUILT_AREA_M2,
                REGISTRATION_DATE
        );
    }

    private PropertyIO savedPropertyIOSemOrganizacao() {
        return new PropertyIO(
                PROPERTY_ID,
                PROPERTY_NAME,
                PROPERTY_TYPE,
                CLASSIFICATION,
                ADDRESS_ID,
                null,
                null,
                REGISTRATION_DATE
        );
    }

    private Address address() {
        Address address = new Address();
        address.setId(ADDRESS_ID);
        return address;
    }

    private Organization organization() {
        Organization organization = new Organization();
        organization.setId(ORGANIZATION_ID);
        return organization;
    }

    private PropertyClassificationEntity classificationEntity() {
        return new PropertyClassificationEntity(1, CLASSIFICATION, PropertyClassificationGroup.RESIDENCIAL);
    }

    private Property property(Integer id, String name, Integer organizationId, BigDecimal builtAreaM2) {
        Organization organization = organizationId != null ? organization() : null;
        return new Property(
                id,
                name,
                PROPERTY_TYPE,
                classificationEntity(),
                address(),
                organization,
                builtAreaM2,
                REGISTRATION_DATE
        );
    }
}
