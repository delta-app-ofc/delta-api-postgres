package br.com.delta.delta_api_postgres.modules.analytics.repository;

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
        ORDER BY rank_efficiency
        """, nativeQuery = true)
    List<PropertyRankingProjection> findPropertyRanking();

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
}
