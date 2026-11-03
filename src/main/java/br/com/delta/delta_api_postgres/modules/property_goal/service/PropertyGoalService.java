package br.com.delta.delta_api_postgres.modules.property_goal.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.auth.security.UserDataAccess;
import br.com.delta.delta_api_postgres.modules.property_goal.dto.io.PropertyGoalIO;
import br.com.delta.delta_api_postgres.modules.property_goal.entity.PropertyGoal;
import br.com.delta.delta_api_postgres.modules.property_goal.repository.PropertyGoalRepository;
import br.com.delta.delta_api_postgres.modules.property.repository.PropertyRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PropertyGoalService {
    private final UserDataAccess access;
    private final PropertyRepository propertyRepository;
    private final PropertyGoalRepository goalRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public PropertyGoalIO find(Integer propertyId) {
        access.property(propertyId);
        PropertyGoal goal = goalRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta nao encontrada"));
        return toIO(goal);
    }

    /** Grava a meta e atualiza a dimensao de instalacoes para que as views dw reflitam a mudanca sem esperar o ETL. */
    @Transactional
    public PropertyGoalIO save(Integer propertyId, BigDecimal monthlyTargetLiters) {
        access.property(propertyId);
        if (!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Propriedade nao encontrada");
        }
        PropertyGoal goal = goalRepository.findById(propertyId).orElseGet(() -> {
            PropertyGoal created = new PropertyGoal();
            created.setPropertyId(propertyId);
            return created;
        });
        goal.setMonthlyTargetLiters(monthlyTargetLiters);
        goal.setUpdatedAt(LocalDateTime.now());
        PropertyGoal saved = goalRepository.saveAndFlush(goal);
        entityManager.createNativeQuery("CALL silver.sp_load_dm_property()").executeUpdate();
        entityManager.createNativeQuery("CALL gold.sp_load_dm_property()").executeUpdate();
        return toIO(saved);
    }

    private PropertyGoalIO toIO(PropertyGoal goal) {
        return new PropertyGoalIO(goal.getPropertyId(), goal.getMonthlyTargetLiters(), goal.getUpdatedAt());
    }
}
