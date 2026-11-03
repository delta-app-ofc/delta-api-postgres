package br.com.delta.delta_api_postgres.modules.analytics.repository;

import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.AlertProjection;
import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.OrganizationMonthlyKpiProjection;
import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.PropertyMonthlyProjection;
import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.MonitoringHourlyProjection;
import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.DeviceStatusProjection;
import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.DeviceSummaryProjection;
import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.LeakProjection;
import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.OrganizationEsgProjection;
import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.AuditHistoryChainProjection;
import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.CapexComparisonProjection;
import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.ConsumptionDailyProjection;
import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.ConsumptionDistributionProjection;
import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.MonthlyVariationProjection;
import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.PropertyRankingProjection;
import br.com.delta.delta_api_postgres.modules.analytics.repository.projection.ResidentialEfficiencyRankingProjection;
import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio somente leitura das views de BI (schema dw). As views nao tem
 * create/update/delete e algumas nao tem uma coluna-chave natural, entao nao
 * fazem sentido como entidade JPA propria. Por isso os metodos ficam aqui como
 * @Query nativas anexadas a JpaRepository<Property, Integer> - mesmo padrao ja
 * usado em PropertyRepository.calculateWaterCost, que tambem e uma consulta
 * nativa sem relacao direta com o CRUD da entidade declarada.
 */
public interface AnalyticsRepository extends JpaRepository<Property, Integer> {

    @Query(value = """
        SELECT
            property_id           AS "propertyId",
            property_name         AS "propertyName",
            full_date             AS "fullDate",
            total_liters          AS "totalLiters",
            cost_value            AS "costValue",
            running_total_liters  AS "runningTotalLiters",
            moving_avg_7d_liters  AS "movingAvg7dLiters"
        FROM dw.vw_ft_consumption_daily
        WHERE property_id = :propertyId
        ORDER BY full_date
        """, nativeQuery = true)
    List<ConsumptionDailyProjection> findConsumptionDailyByPropertyId(@Param("propertyId") Integer propertyId);

    @Query(value = """
        SELECT
            property_id                AS "propertyId",
            property_name              AS "propertyName",
            total_liters                AS "totalLiters",
            total_cost                  AS "totalCost",
            liters_per_m2                AS "litersPerM2",
            rank_efficiency              AS "rankEfficiency",
            rank_cost                    AS "rankCost",
            consumption_quartile         AS "consumptionQuartile",
            consumption_percent_rank     AS "consumptionPercentRank"
        FROM dw.vw_ft_property_ranking
        WHERE property_id IN (SELECT property_id FROM tb_user_property WHERE user_id = :userId)
        ORDER BY rank_efficiency
        """, nativeQuery = true)
    List<PropertyRankingProjection> findPropertyRanking(@Param("userId") Integer userId);

    @Query(value = """
        SELECT
            property_id             AS "propertyId",
            property_name           AS "propertyName",
            reference_month         AS "referenceMonth",
            total_liters            AS "totalLiters",
            previous_month_liters   AS "previousMonthLiters",
            variation_pct           AS "variationPct"
        FROM dw.vw_ft_monthly_variation
        WHERE property_id = :propertyId
        ORDER BY reference_month
        """, nativeQuery = true)
    List<MonthlyVariationProjection> findMonthlyVariationByPropertyId(@Param("propertyId") Integer propertyId);

    @Query(value = """
        SELECT
            property_key         AS "propertyKey",
            total_liters         AS "totalLiters",
            distribution_bucket  AS "distributionBucket",
            min_liters           AS "minLiters",
            q1_liters            AS "q1Liters",
            median_liters        AS "medianLiters",
            q3_liters            AS "q3Liters",
            max_liters           AS "maxLiters"
        FROM dw.vw_ft_consumption_distribution
        ORDER BY property_key
        """, nativeQuery = true)
    List<ConsumptionDistributionProjection> findConsumptionDistribution();

    @Query(value = """
        SELECT
            scenario_id            AS "scenarioId",
            name                   AS "name",
            investment_value       AS "investmentValue",
            reduction_pct          AS "reductionPct",
            annual_savings_value   AS "annualSavingsValue",
            payback_months         AS "paybackMonths",
            rank_by_payback        AS "rankByPayback"
        FROM dw.vw_ft_capex_comparison
        ORDER BY rank_by_payback
        """, nativeQuery = true)
    List<CapexComparisonProjection> findCapexComparison();

    @Query(value = """
        SELECT
            user_id                     AS "userId",
            person_name                 AS "personName",
            reference_month             AS "referenceMonth",
            m3_value                    AS "m3Value",
            total_value                 AS "totalValue",
            rank_efficiency             AS "rankEfficiency",
            consumption_percent_rank    AS "consumptionPercentRank"
        FROM dw.vw_ft_residential_efficiency_ranking
        WHERE user_id = :userId
        ORDER BY reference_month
        """, nativeQuery = true)
    List<ResidentialEfficiencyRankingProjection> findResidentialEfficiencyRankingByUserId(@Param("userId") Integer userId);

    @Query(value = """
        SELECT
            log_id           AS "logId",
            region_rate_id   AS "regionRateId",
            m3_value         AS "m3Value",
            operation        AS "operation",
            executed_by      AS "executedBy",
            executed_at      AS "executedAt",
            level            AS "level"
        FROM dw.vw_audit_history_chain
        WHERE :regionRateId IS NULL OR region_rate_id = :regionRateId
        ORDER BY region_rate_id, level
        """, nativeQuery = true)
    List<AuditHistoryChainProjection> findAuditHistory(@Param("regionRateId") Integer regionRateId);

    @Query(value = """
        SELECT
            organization_id AS "organizationId",
            reference_month AS "referenceMonth",
            total_liters AS "totalLiters",
            total_cost AS "totalCost",
            previous_month_liters AS "previousMonthLiters",
            variation_pct AS "variationPct",
            projected_liters AS "projectedLiters",
            projected_cost AS "projectedCost",
            units_total AS "unitsTotal",
            units_on_target AS "unitsOnTarget",
            units_attention AS "unitsAttention",
            units_critical AS "unitsCritical",
            avg_goal_pct AS "avgGoalPct",
            estimated_savings_value AS "estimatedSavingsValue",
            saved_liters AS "savedLiters",
            min_days_with_data AS "minDaysWithData",
            is_complete_month AS "isCompleteMonth"
        FROM dw.vw_ft_organization_monthly_kpi
        WHERE organization_id IN (SELECT p.organization_id FROM tb_property p JOIN tb_user_property up ON up.property_id = p.id WHERE up.user_id = :userId UNION SELECT uo.organization_id FROM tb_user_organization uo WHERE uo.user_id = :userId)
        ORDER BY reference_month DESC, organization_id
        """, nativeQuery = true)
    List<OrganizationMonthlyKpiProjection> findOrganizationMonthlyKpi(@Param("userId") Integer userId);

    @Query(value = """
        SELECT
            property_id AS "propertyId",
            property_name AS "propertyName",
            organization_id AS "organizationId",
            city AS "city",
            state AS "state",
            region_name AS "regionName",
            latitude AS "latitude",
            longitude AS "longitude",
            built_area_m2 AS "builtAreaM2",
            reference_month AS "referenceMonth",
            days_with_data AS "daysWithData",
            is_complete_month AS "isCompleteMonth",
            total_liters AS "totalLiters",
            total_cost AS "totalCost",
            liters_per_m2 AS "litersPerM2",
            previous_month_liters AS "previousMonthLiters",
            variation_pct AS "variationPct",
            monthly_target_liters AS "monthlyTargetLiters",
            goal_pct AS "goalPct",
            goal_status AS "goalStatus",
            estimated_savings_value AS "estimatedSavingsValue",
            saved_liters AS "savedLiters"
        FROM dw.vw_ft_property_monthly
        WHERE property_id IN (SELECT property_id FROM tb_user_property WHERE user_id = :userId)
          AND (CAST(:month AS date) IS NULL OR reference_month = CAST(:month AS date))
        ORDER BY reference_month DESC, property_id
        """, nativeQuery = true)
    List<PropertyMonthlyProjection> findPropertyMonthly(@Param("userId") Integer userId, @Param("month") LocalDate month);

    @Query(value = """
        SELECT
            organization_id AS "organizationId",
            property_id AS "propertyId",
            property_name AS "propertyName",
            window_minutes AS "windowMinutes",
            reading_hour AS "readingHour",
            total_liters AS "totalLiters",
            avg_flow_lmin AS "avgFlowLmin",
            readings_count AS "readingsCount"
        FROM dw.vw_ft_monitoring_hourly
        WHERE property_id IN (SELECT property_id FROM tb_user_property WHERE user_id = :userId)
          AND reading_hour > (SELECT MAX(m.reading_hour) FROM dw.vw_ft_monitoring_hourly m WHERE m.property_id IN (SELECT property_id FROM tb_user_property WHERE user_id = :userId)) - make_interval(hours => :hours)
          AND (CAST(:windowMinutes AS integer) IS NULL OR window_minutes = CAST(:windowMinutes AS integer))
        ORDER BY reading_hour, property_id
        """, nativeQuery = true)
    List<MonitoringHourlyProjection> findMonitoringHourly(@Param("userId") Integer userId, @Param("hours") Integer hours, @Param("windowMinutes") Integer windowMinutes);

    @Query(value = """
        SELECT
            organization_id AS "organizationId",
            property_id AS "propertyId",
            property_name AS "propertyName",
            region_name AS "regionName",
            device_id AS "deviceId",
            is_active AS "isActive",
            connectivity_status AS "connectivityStatus",
            is_online AS "isOnline",
            battery_level AS "batteryLevel",
            is_low_battery AS "isLowBattery",
            wifi_signal_rssi AS "wifiSignalRssi",
            firmware_version AS "firmwareVersion",
            last_ping_at AS "lastPingAt",
            unavailability_reason AS "unavailabilityReason"
        FROM dw.vw_ft_device_status
        WHERE property_id IN (SELECT property_id FROM tb_user_property WHERE user_id = :userId)
        ORDER BY property_name, device_id
        """, nativeQuery = true)
    List<DeviceStatusProjection> findDeviceStatus(@Param("userId") Integer userId);

    @Query(value = """
        SELECT
            organization_id AS "organizationId",
            properties_total AS "propertiesTotal",
            devices_paired AS "devicesPaired",
            devices_online AS "devicesOnline",
            devices_low_battery AS "devicesLowBattery",
            properties_without_device AS "propertiesWithoutDevice"
        FROM dw.vw_ft_organization_device_summary
        WHERE organization_id IN (SELECT p.organization_id FROM tb_property p JOIN tb_user_property up ON up.property_id = p.id WHERE up.user_id = :userId UNION SELECT uo.organization_id FROM tb_user_organization uo WHERE uo.user_id = :userId)
        ORDER BY organization_id
        """, nativeQuery = true)
    List<DeviceSummaryProjection> findDeviceSummary(@Param("userId") Integer userId);

    @Query(value = """
        SELECT
            alert_key AS "alertKey",
            organization_id AS "organizationId",
            property_id AS "propertyId",
            property_name AS "propertyName",
            device_id AS "deviceId",
            severity AS "severity",
            triggered_at AS "triggeredAt",
            resolved_at AS "resolvedAt",
            leak_status AS "leakStatus",
            duration_minutes AS "durationMinutes",
            estimated_loss_liters AS "estimatedLossLiters",
            estimated_loss_cost AS "estimatedLossCost"
        FROM dw.vw_ft_leak
        WHERE property_id IN (SELECT property_id FROM tb_user_property WHERE user_id = :userId)
          AND (CAST(:status AS text) IS NULL OR leak_status = CAST(:status AS text))
        ORDER BY triggered_at DESC
        """, nativeQuery = true)
    List<LeakProjection> findLeaks(@Param("userId") Integer userId, @Param("status") String status);

    @Query(value = """
        SELECT
            organization_id AS "organizationId",
            reference_month AS "referenceMonth",
            saved_liters_month AS "savedLitersMonth",
            saved_liters_year_projection AS "savedLitersYearProjection",
            tank_trucks_equivalent AS "tankTrucksEquivalent",
            family_months_equivalent AS "familyMonthsEquivalent",
            units_total AS "unitsTotal",
            units_on_target AS "unitsOnTarget",
            avg_goal_pct AS "avgGoalPct",
            estimated_savings_value AS "estimatedSavingsValue"
        FROM dw.vw_ft_organization_esg
        WHERE organization_id IN (SELECT p.organization_id FROM tb_property p JOIN tb_user_property up ON up.property_id = p.id WHERE up.user_id = :userId UNION SELECT uo.organization_id FROM tb_user_organization uo WHERE uo.user_id = :userId)
        ORDER BY reference_month DESC, organization_id
        """, nativeQuery = true)
    List<OrganizationEsgProjection> findOrganizationEsg(@Param("userId") Integer userId);

    @Query(value = """
        SELECT
            alert_key AS "alertKey",
            property_id AS "propertyId",
            property_name AS "propertyName",
            device_id AS "deviceId",
            alert_type AS "alertType",
            severity AS "severity",
            triggered_at AS "triggeredAt",
            resolved_at AS "resolvedAt",
            resolution_minutes AS "resolutionMinutes"
        FROM dw.vw_ft_alert
        WHERE property_id IN (SELECT property_id FROM tb_user_property WHERE user_id = :userId)
          AND (CAST(:severity AS text) IS NULL OR severity = CAST(:severity AS text))
          AND (CAST(:alertType AS text) IS NULL OR alert_type = CAST(:alertType AS text))
          AND (CAST(:onlyActive AS boolean) IS NOT TRUE OR resolved_at IS NULL)
        ORDER BY triggered_at DESC
        """, nativeQuery = true)
    List<AlertProjection> findAlerts(@Param("userId") Integer userId, @Param("severity") String severity, @Param("alertType") String alertType, @Param("onlyActive") Boolean onlyActive);
}

