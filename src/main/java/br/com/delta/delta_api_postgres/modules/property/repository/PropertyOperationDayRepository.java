package br.com.delta.delta_api_postgres.modules.property.repository;

import br.com.delta.delta_api_postgres.modules.property.entity.PropertyOperationDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PropertyOperationDayRepository extends JpaRepository<PropertyOperationDay, Integer> {
    // Nomes dos dias (SEGUNDA, TERÇA...) em que o imóvel funciona.
    @Query("select d.name from PropertyOperationDay o, DayOfWeek d "
            + "where o.dayOfWeekId = d.id and o.propertyId = :propertyId")
    List<String> findDayNamesByPropertyId(@Param("propertyId") Integer propertyId);
}
