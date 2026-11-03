package br.com.delta.delta_api_postgres.modules.property.repository;

import br.com.delta.delta_api_postgres.modules.property.entity.PropertyShift;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PropertyShiftRepository extends JpaRepository<PropertyShift, Integer> {
    List<PropertyShift> findByPropertyIdOrderByStartTime(Integer propertyId);
}
