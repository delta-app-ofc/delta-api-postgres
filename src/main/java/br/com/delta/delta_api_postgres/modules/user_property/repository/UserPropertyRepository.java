package br.com.delta.delta_api_postgres.modules.user_property.repository;

import br.com.delta.delta_api_postgres.modules.user_property.entity.UserProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPropertyRepository
        extends JpaRepository<UserProperty, Integer> {
    void deleteByProperty_Id(Integer propertyId);

    List<UserProperty> findByUserId(Integer userId);

    List<UserProperty> findByProperty_Id(Integer propertyId);

    boolean existsByUserIdAndProperty_Id(
            Integer userId,
            Integer propertyId
    );
}
