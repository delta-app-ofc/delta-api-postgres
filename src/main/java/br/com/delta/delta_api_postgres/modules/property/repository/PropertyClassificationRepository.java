package br.com.delta.delta_api_postgres.modules.property.repository;

import br.com.delta.delta_api_postgres.modules.property.entity.PropertyClassificationEntity;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyClassification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PropertyClassificationRepository
        extends JpaRepository<PropertyClassificationEntity, Integer> {

    Optional<PropertyClassificationEntity> findByName(PropertyClassification name);
}
