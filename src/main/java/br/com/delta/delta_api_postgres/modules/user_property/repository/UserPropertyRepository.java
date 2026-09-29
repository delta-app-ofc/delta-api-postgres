package br.com.delta.delta_api_postgres.modules.user_property.repository;

import br.com.delta.delta_api_postgres.modules.user_property.entity.UserProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPropertyRepository
        extends JpaRepository<UserProperty, Integer> {

    List<UserProperty> findByUserId(Integer userId);

    boolean existsByUserIdAndProperty_Id(
            Integer userId,
            Integer propertyId
    );
}
