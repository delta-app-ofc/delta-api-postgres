package br.com.delta.delta_api_postgres.modules.user_property.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceAlreadyExistsException;
import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyClassification;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyType;
import br.com.delta.delta_api_postgres.modules.property.repository.PropertyRepository;
import br.com.delta.delta_api_postgres.modules.user_property.dto.io.UserPropertyIO;
import br.com.delta.delta_api_postgres.modules.user_property.entity.UserProperty;
import br.com.delta.delta_api_postgres.modules.user_property.mapper.UserPropertyMapper;
import br.com.delta.delta_api_postgres.modules.user_property.repository.UserPropertyRepository;
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
class UserPropertyServiceTest {

    private static final Integer USER_PROPERTY_ID = 1;
    private static final Integer USER_ID = 10;
    private static final Integer OTHER_USER_ID = 99;
    private static final Integer PROPERTY_ID = 100;
    private static final Integer NEW_PROPERTY_ID = 200;
    private static final LocalDate ASSOCIATION_DATE = LocalDate.of(2026, 1, 15);

    @Mock
    private UserPropertyRepository userPropertyRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private UserPropertyMapper userPropertyMapper;

    @InjectMocks
    private UserPropertyService userPropertyService;

    @Test
    void create_quandoDadosValidos_deveSalvarERetornarAssociacao() {
        // Arrange
        UserPropertyIO input = inputUserPropertyIO(PROPERTY_ID);
        Property property = property(PROPERTY_ID, "Casa principal");
        UserProperty userProperty = userProperty(null, USER_ID, property);
        UserProperty savedUserProperty = userProperty(USER_PROPERTY_ID, USER_ID, property);
        UserPropertyIO expected = savedUserPropertyIO(PROPERTY_ID, "Casa principal");

        when(userPropertyRepository.existsByUserIdAndProperty_Id(USER_ID, PROPERTY_ID))
                .thenReturn(false);
        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property));
        when(userPropertyMapper.toEntity(input, property)).thenReturn(userProperty);
        when(userPropertyRepository.save(userProperty)).thenReturn(savedUserProperty);
        when(userPropertyMapper.toIO(savedUserProperty)).thenReturn(expected);

        // Act
        UserPropertyIO result = userPropertyService.create(input);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(userPropertyRepository).save(userProperty);
        verify(userPropertyMapper).toIO(savedUserProperty);
    }

    @Test
    void create_quandoAssociacaoJaExiste_deveLancarResourceAlreadyExistsException() {
        UserPropertyIO input = inputUserPropertyIO(PROPERTY_ID);
        when(userPropertyRepository.existsByUserIdAndProperty_Id(USER_ID, PROPERTY_ID))
                .thenReturn(true);

        assertThatThrownBy(() -> userPropertyService.create(input))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Esse imóvel já está associado a esse usuário");

        verify(userPropertyRepository, never()).save(any());
        verifyNoInteractions(propertyRepository, userPropertyMapper);
    }

    @Test
    void create_quandoImovelNaoExiste_deveLancarResourceNotFoundException() {
        UserPropertyIO input = inputUserPropertyIO(PROPERTY_ID);
        when(userPropertyRepository.existsByUserIdAndProperty_Id(USER_ID, PROPERTY_ID))
                .thenReturn(false);
        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userPropertyService.create(input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Imóvel não encontrado");

        verify(userPropertyRepository, never()).save(any());
        verifyNoInteractions(userPropertyMapper);
    }

    @Test
    void findAll_quandoExistemAssociacoes_deveRetornarTodasDoUsuarioMapeadas() {
        UserProperty first = userProperty(
                USER_PROPERTY_ID,
                USER_ID,
                property(PROPERTY_ID, "Casa principal")
        );
        UserProperty second = userProperty(
                2,
                USER_ID,
                property(NEW_PROPERTY_ID, "Casa de praia")
        );
        UserPropertyIO firstIO = savedUserPropertyIO(PROPERTY_ID, "Casa principal");
        UserPropertyIO secondIO = new UserPropertyIO(
                2,
                USER_ID,
                NEW_PROPERTY_ID,
                "Casa de praia",
                ASSOCIATION_DATE
        );

        when(userPropertyRepository.findByUserId(USER_ID)).thenReturn(List.of(first, second));
        when(userPropertyMapper.toIO(first)).thenReturn(firstIO);
        when(userPropertyMapper.toIO(second)).thenReturn(secondIO);

        List<UserPropertyIO> result = userPropertyService.findAll(USER_ID);

        assertThat(result).containsExactly(firstIO, secondIO);
        verify(userPropertyMapper).toIO(first);
        verify(userPropertyMapper).toIO(second);
    }

    @Test
    void findAll_quandoNaoExistemAssociacoes_deveRetornarListaVazia() {
        when(userPropertyRepository.findByUserId(USER_ID)).thenReturn(List.of());

        List<UserPropertyIO> result = userPropertyService.findAll(USER_ID);

        assertThat(result).isEmpty();
        verifyNoInteractions(userPropertyMapper);
    }

    @Test
    void findById_quandoAssociacaoExisteEPertenceAoUsuario_deveRetornarAssociacaoMapeada() {
        UserProperty userProperty = userProperty(
                USER_PROPERTY_ID,
                USER_ID,
                property(PROPERTY_ID, "Casa principal")
        );
        UserPropertyIO expected = savedUserPropertyIO(PROPERTY_ID, "Casa principal");

        when(userPropertyRepository.findById(USER_PROPERTY_ID)).thenReturn(Optional.of(userProperty));
        when(userPropertyMapper.toIO(userProperty)).thenReturn(expected);

        UserPropertyIO result = userPropertyService.findById(USER_ID, USER_PROPERTY_ID);

        assertThat(result).isEqualTo(expected);
        verify(userPropertyMapper).toIO(userProperty);
    }

    @Test
    void findById_quandoAssociacaoNaoExiste_deveLancarResourceNotFoundException() {
        when(userPropertyRepository.findById(USER_PROPERTY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userPropertyService.findById(USER_ID, USER_PROPERTY_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Associação não encontrada");

        verifyNoInteractions(userPropertyMapper);
    }

    @Test
    void findById_quandoAssociacaoPertenceAOutroUsuario_deveLancarResourceNotFoundException() {
        UserProperty userProperty = userProperty(
                USER_PROPERTY_ID,
                OTHER_USER_ID,
                property(PROPERTY_ID, "Casa principal")
        );
        when(userPropertyRepository.findById(USER_PROPERTY_ID)).thenReturn(Optional.of(userProperty));

        assertThatThrownBy(() -> userPropertyService.findById(USER_ID, USER_PROPERTY_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Associação não encontrada");

        verifyNoInteractions(userPropertyMapper);
    }

    @Test
    void update_quandoNovoImovelValido_deveAtualizarERetornarAssociacao() {
        Property currentProperty = property(PROPERTY_ID, "Casa principal");
        Property newProperty = property(NEW_PROPERTY_ID, "Casa de praia");
        UserProperty userProperty = userProperty(USER_PROPERTY_ID, USER_ID, currentProperty);
        UserPropertyIO input = inputUserPropertyIO(NEW_PROPERTY_ID);
        UserPropertyIO expected = savedUserPropertyIO(NEW_PROPERTY_ID, "Casa de praia");

        when(userPropertyRepository.findById(USER_PROPERTY_ID)).thenReturn(Optional.of(userProperty));
        when(userPropertyRepository.existsByUserIdAndProperty_Id(USER_ID, NEW_PROPERTY_ID))
                .thenReturn(false);
        when(propertyRepository.findById(NEW_PROPERTY_ID)).thenReturn(Optional.of(newProperty));
        when(userPropertyMapper.toIO(userProperty)).thenReturn(expected);

        UserPropertyIO result = userPropertyService.update(USER_ID, USER_PROPERTY_ID, input);

        assertThat(result).isEqualTo(expected);
        assertThat(userProperty.getProperty()).isSameAs(newProperty);
        verify(userPropertyRepository, never()).save(any());
        verify(userPropertyMapper).toIO(userProperty);
    }

    @Test
    void update_quandoImovelNaoMudou_deveAtualizarSemVerificarDuplicidade() {
        Property property = property(PROPERTY_ID, "Casa principal");
        UserProperty userProperty = userProperty(USER_PROPERTY_ID, USER_ID, property);
        UserPropertyIO input = inputUserPropertyIO(PROPERTY_ID);
        UserPropertyIO expected = savedUserPropertyIO(PROPERTY_ID, "Casa principal");

        when(userPropertyRepository.findById(USER_PROPERTY_ID)).thenReturn(Optional.of(userProperty));
        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property));
        when(userPropertyMapper.toIO(userProperty)).thenReturn(expected);

        UserPropertyIO result = userPropertyService.update(USER_ID, USER_PROPERTY_ID, input);

        assertThat(result).isEqualTo(expected);
        verify(userPropertyRepository, never())
                .existsByUserIdAndProperty_Id(USER_ID, PROPERTY_ID);
        verify(userPropertyRepository, never()).save(any());
    }

    @Test
    void update_quandoAssociacaoNaoExiste_deveLancarResourceNotFoundException() {
        UserPropertyIO input = inputUserPropertyIO(NEW_PROPERTY_ID);
        when(userPropertyRepository.findById(USER_PROPERTY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userPropertyService.update(USER_ID, USER_PROPERTY_ID, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Associação não encontrada");

        verifyNoInteractions(propertyRepository, userPropertyMapper);
    }

    @Test
    void update_quandoAssociacaoPertenceAOutroUsuario_deveLancarResourceNotFoundException() {
        UserProperty userProperty = userProperty(
                USER_PROPERTY_ID,
                OTHER_USER_ID,
                property(PROPERTY_ID, "Casa principal")
        );
        UserPropertyIO input = inputUserPropertyIO(NEW_PROPERTY_ID);
        when(userPropertyRepository.findById(USER_PROPERTY_ID)).thenReturn(Optional.of(userProperty));

        assertThatThrownBy(() -> userPropertyService.update(USER_ID, USER_PROPERTY_ID, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Associação não encontrada");

        verifyNoInteractions(propertyRepository, userPropertyMapper);
    }

    @Test
    void update_quandoNovoImovelJaEstaAssociado_deveLancarResourceAlreadyExistsException() {
        UserProperty userProperty = userProperty(
                USER_PROPERTY_ID,
                USER_ID,
                property(PROPERTY_ID, "Casa principal")
        );
        UserPropertyIO input = inputUserPropertyIO(NEW_PROPERTY_ID);

        when(userPropertyRepository.findById(USER_PROPERTY_ID)).thenReturn(Optional.of(userProperty));
        when(userPropertyRepository.existsByUserIdAndProperty_Id(USER_ID, NEW_PROPERTY_ID))
                .thenReturn(true);

        assertThatThrownBy(() -> userPropertyService.update(USER_ID, USER_PROPERTY_ID, input))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Esse imóvel já está associado a esse usuário");

        verifyNoInteractions(propertyRepository, userPropertyMapper);
    }

    @Test
    void update_quandoNovoImovelNaoExiste_deveLancarResourceNotFoundException() {
        UserProperty userProperty = userProperty(
                USER_PROPERTY_ID,
                USER_ID,
                property(PROPERTY_ID, "Casa principal")
        );
        UserPropertyIO input = inputUserPropertyIO(NEW_PROPERTY_ID);

        when(userPropertyRepository.findById(USER_PROPERTY_ID)).thenReturn(Optional.of(userProperty));
        when(userPropertyRepository.existsByUserIdAndProperty_Id(USER_ID, NEW_PROPERTY_ID))
                .thenReturn(false);
        when(propertyRepository.findById(NEW_PROPERTY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userPropertyService.update(USER_ID, USER_PROPERTY_ID, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Imóvel não encontrado");

        verifyNoInteractions(userPropertyMapper);
    }

    @Test
    void delete_quandoAssociacaoExisteEPertenceAoUsuario_deveExcluirAssociacao() {
        UserProperty userProperty = userProperty(
                USER_PROPERTY_ID,
                USER_ID,
                property(PROPERTY_ID, "Casa principal")
        );
        when(userPropertyRepository.findById(USER_PROPERTY_ID)).thenReturn(Optional.of(userProperty));

        userPropertyService.delete(USER_ID, USER_PROPERTY_ID);

        verify(userPropertyRepository).delete(userProperty);
    }

    @Test
    void delete_quandoAssociacaoNaoExiste_deveLancarResourceNotFoundException() {
        when(userPropertyRepository.findById(USER_PROPERTY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userPropertyService.delete(USER_ID, USER_PROPERTY_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Associação não encontrada");

        verify(userPropertyRepository, never()).delete(any());
    }

    @Test
    void delete_quandoAssociacaoPertenceAOutroUsuario_deveLancarResourceNotFoundException() {
        UserProperty userProperty = userProperty(
                USER_PROPERTY_ID,
                OTHER_USER_ID,
                property(PROPERTY_ID, "Casa principal")
        );
        when(userPropertyRepository.findById(USER_PROPERTY_ID)).thenReturn(Optional.of(userProperty));

        assertThatThrownBy(() -> userPropertyService.delete(USER_ID, USER_PROPERTY_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Associação não encontrada");

        verify(userPropertyRepository, never()).delete(any());
    }

    private UserPropertyIO inputUserPropertyIO(Integer propertyId) {
        return new UserPropertyIO(null, USER_ID, propertyId, null, null);
    }

    private UserPropertyIO savedUserPropertyIO(Integer propertyId, String propertyName) {
        return new UserPropertyIO(
                USER_PROPERTY_ID,
                USER_ID,
                propertyId,
                propertyName,
                ASSOCIATION_DATE
        );
    }

    private Property property(Integer id, String name) {
        return new Property(
                id,
                name,
                PropertyType.CASA,
                PropertyClassification.RESIDENCIAL,
                null,
                LocalDate.of(2026, 1, 1)
        );
    }

    private UserProperty userProperty(Integer id, Integer userId, Property property) {
        UserProperty userProperty = new UserProperty();
        userProperty.setId(id);
        userProperty.setUserId(userId);
        userProperty.setProperty(property);
        userProperty.setAssociationDate(ASSOCIATION_DATE);
        return userProperty;
    }
}
