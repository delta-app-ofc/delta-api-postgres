package br.com.delta.delta_api_postgres.modules.property_goal.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.auth.security.UserDataAccess;
import br.com.delta.delta_api_postgres.modules.property.repository.PropertyRepository;
import br.com.delta.delta_api_postgres.modules.property_goal.dto.io.PropertyGoalIO;
import br.com.delta.delta_api_postgres.modules.property_goal.entity.PropertyGoal;
import br.com.delta.delta_api_postgres.modules.property_goal.repository.PropertyGoalRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyGoalServiceTest {
    private static final Integer PROPERTY_ID = 157;

    @Mock private UserDataAccess access;
    @Mock private PropertyRepository propertyRepository;
    @Mock private PropertyGoalRepository goalRepository;
    @Mock private EntityManager entityManager;
    @InjectMocks private PropertyGoalService service;

    @Test
    void createsTheGoalChecksAccessAndRefreshesTheDimensions() {
        Query refresh = mock(Query.class);
        when(propertyRepository.existsById(PROPERTY_ID)).thenReturn(true);
        when(goalRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty());
        when(goalRepository.saveAndFlush(any(PropertyGoal.class))).thenAnswer(call -> call.getArgument(0));
        when(entityManager.createNativeQuery(any(String.class))).thenReturn(refresh);

        PropertyGoalIO result = service.save(PROPERTY_ID, new BigDecimal("7500"));

        ArgumentCaptor<PropertyGoal> saved = ArgumentCaptor.forClass(PropertyGoal.class);
        verify(goalRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getPropertyId()).isEqualTo(PROPERTY_ID);
        assertThat(saved.getValue().getMonthlyTargetLiters()).isEqualByComparingTo("7500");
        assertThat(result.monthlyTargetLiters()).isEqualByComparingTo("7500");
        var order = inOrder(access, entityManager);
        order.verify(access).property(PROPERTY_ID);
        order.verify(entityManager).createNativeQuery("CALL silver.sp_load_dm_property()");
        order.verify(entityManager).createNativeQuery("CALL gold.sp_load_dm_property()");
    }

    @Test
    void updatesAnExistingGoalInsteadOfCreatingAnother() {
        Query refresh = mock(Query.class);
        PropertyGoal existing = new PropertyGoal();
        existing.setPropertyId(PROPERTY_ID);
        existing.setMonthlyTargetLiters(new BigDecimal("3000"));
        when(propertyRepository.existsById(PROPERTY_ID)).thenReturn(true);
        when(goalRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(existing));
        when(goalRepository.saveAndFlush(existing)).thenReturn(existing);
        when(entityManager.createNativeQuery(any(String.class))).thenReturn(refresh);

        service.save(PROPERTY_ID, new BigDecimal("9000"));

        assertThat(existing.getMonthlyTargetLiters()).isEqualByComparingTo("9000");
        assertThat(existing.getUpdatedAt()).isNotNull();
    }

    @Test
    void rejectsAnUnknownPropertyWithoutWriting() {
        when(propertyRepository.existsById(PROPERTY_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.save(PROPERTY_ID, BigDecimal.TEN))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(goalRepository, never()).saveAndFlush(any());
    }

    @Test
    void reportsAMissingGoalOnRead() {
        when(goalRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.find(PROPERTY_ID)).isInstanceOf(ResourceNotFoundException.class);
        verify(access).property(PROPERTY_ID);
    }
}
