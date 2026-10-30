package br.com.delta.delta_api_postgres.modules.property.repository;

import java.util.List;

import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import br.com.delta.delta_api_postgres.modules.property.repository.projection.WaterCostProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PropertyRepository extends JpaRepository<Property, Integer> {
    @Query("select distinct p from Property p where exists (select u.id from UserProperty u where u.property = p and u.userId = :userId)")
    List<Property> findForUser(@Param("userId") Integer userId);

    public boolean existsByName(String name);

    @Query(value = """
        SELECT
            property_id AS "propertyId",
            region_id AS "regionId",
            consumption_m3 AS "consumptionM3",
            rate_per_m3 AS "ratePerM3",
            estimated_cost AS "estimatedCost",
            reference_date AS "referenceDate"
        FROM fn_calculate_water_cost(
            :propertyId,
            :consumptionM3,
            COALESCE(CAST(:referenceDate AS DATE), CURRENT_DATE)
        )
        """, nativeQuery = true)
    WaterCostProjection calculateWaterCost(
            @Param("propertyId") Integer propertyId,
            @Param("consumptionM3") BigDecimal consumptionM3,
            @Param("referenceDate") LocalDate referenceDate
    );

    @Procedure(
            procedureName = "sp_register_property",
            outputParameterName = "v_property_id"
    )
    Integer registerProperty(
            @Param("p_user_id") Integer userId,
            @Param("p_name") String name,
            @Param("p_type") String type,
            @Param("p_classification") String classification,
            @Param("p_address_id") Integer addressId,
            @Param("p_organization_id") Integer organizationId,
            @Param("p_built_area_m2") BigDecimal builtAreaM2
    );
}
