package br.com.delta.delta_api_postgres.modules.auth.service;

import java.math.BigDecimal;
import br.com.delta.delta_api_postgres.modules.auth.dto.CompleteRegistrationRequest;
import br.com.delta.delta_api_postgres.modules.auth.entity.AuthUser;
import br.com.delta.delta_api_postgres.modules.auth.repository.AuthUserRepository;
import br.com.delta.delta_api_postgres.modules.address.dto.io.AddressIO;
import br.com.delta.delta_api_postgres.modules.address.dto.request.CreateAddressRequest;
import br.com.delta.delta_api_postgres.modules.address.service.AddressService;
import br.com.delta.delta_api_postgres.modules.habit.dto.io.UserHabitIO;
import br.com.delta.delta_api_postgres.modules.habit.dto.requests.CreateUserHabitRequest;
import br.com.delta.delta_api_postgres.modules.habit.service.UserHabitService;
import br.com.delta.delta_api_postgres.modules.property.dto.io.PropertyIO;
import br.com.delta.delta_api_postgres.modules.property.enums.*;
import br.com.delta.delta_api_postgres.modules.property.service.PropertyService;
import br.com.delta.delta_api_postgres.modules.user_property.dto.io.UserPropertyIO;
import br.com.delta.delta_api_postgres.modules.user_property.service.UserPropertyService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationCompletionServiceTest {
    @Mock AuthUserRepository users;
    @Mock AddressService addresses;
    @Mock PropertyService properties;
    @Mock UserPropertyService associations;
    @Mock UserHabitService habits;
    @Mock br.com.delta.delta_api_postgres.modules.organization.service.OrganizationService organizations;
    @Mock br.com.delta.delta_api_postgres.modules.organization.mapper.OrganizationMapper organizationMapper;
    @InjectMocks RegistrationCompletionService service;
    CompleteRegistrationRequest request;
    AddressIO address;
    PropertyIO property;
    UserHabitIO habit;

    @BeforeEach void setup() {
        var user = new AuthUser(); user.setEnabled(true);
        when(users.lockById(7)).thenReturn(Optional.of(user));
        request = new CompleteRegistrationRequest(new CreateAddressRequest("01001000", "São Paulo", "SP", new BigDecimal("-23.550520"), new BigDecimal("-46.633308")),
                new CompleteRegistrationRequest.PropertyDetails("Casa", PropertyType.CASA,
                        PropertyClassification.RESIDENCIAL_NORMAL, null, null),
                List.of(new CreateUserHabitRequest(2, 3, java.time.LocalTime.of(7, 0), java.time.LocalTime.of(7, 20), new java.math.BigDecimal("45.00"), List.of(1, 3))));
        address = new AddressIO(10, 1, "01001000", "São Paulo", "SP", new BigDecimal("-23.550520"), new BigDecimal("-46.633308"));
        property = new PropertyIO(20, "Casa", PropertyType.CASA,
                PropertyClassification.RESIDENCIAL_NORMAL, 10, null, null, null);
        habit = new UserHabitIO(30, 7, 2, "Hábito", null, null, 3, java.time.LocalTime.of(7, 0), java.time.LocalTime.of(7, 20), new java.math.BigDecimal("45.00"), List.of(1, 3));
    }

    @Test void completesUsingAuthenticatedUserAndCreatesMissingAssociation() {
        when(addresses.create(any())).thenReturn(address);
        when(properties.create(eq(7), any())).thenReturn(property);
        when(habits.create(any())).thenReturn(habit);
        var result = service.complete(7, request);
        assertThat(result.userId()).isEqualTo(7);
        assertThat(result.completed()).isTrue();
        verify(associations).create(new UserPropertyIO(null, 7, 20, null, null));
        verify(habits).create(new UserHabitIO(null, 7, 2, null, null, null, 3, java.time.LocalTime.of(7, 0), java.time.LocalTime.of(7, 20), new java.math.BigDecimal("45.00"), List.of(1, 3)));
    }

    private br.com.delta.delta_api_postgres.modules.organization.dto.request.CreateOrganizationRequest newOrganization() {
        return new br.com.delta.delta_api_postgres.modules.organization.dto.request.CreateOrganizationRequest(
                "Nova Empresa Ltda", "Nova", "12345678000199",
                br.com.delta.delta_api_postgres.modules.organization.enums.OrganizationSegment.INDUSTRIA, 1);
    }

    private br.com.delta.delta_api_postgres.modules.organization.dto.io.OrganizationIO organizationIO() {
        return new br.com.delta.delta_api_postgres.modules.organization.dto.io.OrganizationIO(
                5, "Nova Empresa Ltda", "Nova", "12345678000199",
                br.com.delta.delta_api_postgres.modules.organization.enums.OrganizationSegment.INDUSTRIA, 1, null);
    }

    @Test void createsTheOrganizationAndThePropertyTogetherWhenNewDataIsSent() {
        var withOrganization = new CompleteRegistrationRequest(request.address(), request.property(), request.habits(), newOrganization());
        var io = organizationIO();
        when(organizationMapper.fromCreateRequest(withOrganization.organization())).thenReturn(io);
        when(organizations.createForRegistration(io)).thenReturn(io);
        when(addresses.create(any())).thenReturn(address);
        var linked = new PropertyIO(20, "Casa", PropertyType.CASA, PropertyClassification.RESIDENCIAL_NORMAL, 10, 5, null, null);
        when(properties.createForNewOrganization(eq(7), any())).thenReturn(linked);
        when(habits.create(any())).thenReturn(habit);

        var result = service.complete(7, withOrganization);

        assertThat(result.property().organizationId()).isEqualTo(5);
        var created = ArgumentCaptor.forClass(PropertyIO.class);
        verify(properties).createForNewOrganization(eq(7), created.capture());
        assertThat(created.getValue().organizationId()).isEqualTo(5);
        verify(properties, never()).create(any(), any());
    }

    @Test void rejectsExistingAndNewOrganizationTogetherBeforeWriting() {
        var both = new CompleteRegistrationRequest(request.address(),
                new CompleteRegistrationRequest.PropertyDetails("Casa", PropertyType.CASA, PropertyClassification.RESIDENCIAL_NORMAL, 3, null),
                request.habits(), newOrganization());
        assertThatThrownBy(() -> service.complete(7, both))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        verifyNoInteractions(addresses, properties, associations, habits, organizations);
    }

    @Test void identicalRetryWithANewOrganizationComparesByCnpj() {
        var withOrganization = new CompleteRegistrationRequest(request.address(), request.property(), request.habits(), newOrganization());
        var linked = new PropertyIO(20, "Casa", PropertyType.CASA, PropertyClassification.RESIDENCIAL_NORMAL, 10, 5, null, null);
        when(associations.findAll(7)).thenReturn(List.of(new UserPropertyIO(1, 7, 20, "Casa", null)));
        when(properties.findById(20)).thenReturn(linked);
        when(addresses.findById(10)).thenReturn(address);
        when(habits.findAll(7)).thenReturn(List.of(habit));
        when(organizations.findById(5)).thenReturn(organizationIO());

        assertThat(service.complete(7, withOrganization).property().id()).isEqualTo(20);
        verify(organizations, never()).createForRegistration(any());
        verify(properties, never()).createForNewOrganization(any(), any());
    }

    @Test void identicalRetryDoesNotWriteAnything() {
        when(associations.findAll(7)).thenReturn(List.of(new UserPropertyIO(1, 7, 20, "Casa", null)));
        when(properties.findById(20)).thenReturn(property);
        when(addresses.findById(10)).thenReturn(address);
        when(habits.findAll(7)).thenReturn(List.of(habit));
        assertThat(service.complete(7, request).property().id()).isEqualTo(20);
        verify(addresses, never()).create(any());
        verify(properties, never()).create(any(), any());
        verify(associations, never()).create(any());
        verify(habits, never()).create(any());
    }

    @Test void duplicateHabitsRejectedBeforeWriting() {
        var duplicate = new CompleteRegistrationRequest(request.address(), request.property(),
                List.of(request.habits().get(0), request.habits().get(0)));
        assertThatThrownBy(() -> service.complete(7, duplicate))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        verifyNoInteractions(addresses, properties, associations, habits);
    }

    @Test void failurePropagatesToTransactionalBoundary() {
        when(addresses.create(any())).thenReturn(address);
        when(properties.create(eq(7), any())).thenThrow(new IllegalStateException("procedure failed"));
        assertThatThrownBy(() -> service.complete(7, request)).isInstanceOf(IllegalStateException.class);
        verify(habits, never()).create(any());
        verify(associations, never()).create(any());
    }
}
