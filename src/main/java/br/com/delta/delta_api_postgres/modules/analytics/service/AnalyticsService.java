package br.com.delta.delta_api_postgres.modules.analytics.service;

import br.com.delta.delta_api_postgres.modules.analytics.dto.io.AuditHistoryChainIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.CapexComparisonIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.ConsumptionDailyIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.ConsumptionDistributionIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.MonthlyVariationIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.PropertyRankingIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.ResidentialEfficiencyRankingIO;
import br.com.delta.delta_api_postgres.modules.analytics.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public List<ConsumptionDailyIO> findConsumptionDailyByPropertyId(Integer propertyId) {
        return analyticsRepository.findConsumptionDailyByPropertyId(propertyId).stream()
                .map(p -> new ConsumptionDailyIO(
                        p.getPropertyId(),
                        p.getPropertyName(),
                        p.getFullDate(),
                        p.getTotalLiters(),
                        p.getCostValue(),
                        p.getRunningTotalLiters(),
                        p.getMovingAvg7dLiters()
                ))
                .toList();
    }

    public List<PropertyRankingIO> findPropertyRanking() {
        return analyticsRepository.findPropertyRanking().stream()
                .map(p -> new PropertyRankingIO(
                        p.getPropertyId(),
                        p.getPropertyName(),
                        p.getTotalLiters(),
                        p.getTotalCost(),
                        p.getLitersPerM2(),
                        p.getRankEfficiency(),
                        p.getRankCost(),
                        p.getConsumptionQuartile(),
                        p.getConsumptionPercentRank()
                ))
                .toList();
    }

    public List<MonthlyVariationIO> findMonthlyVariationByPropertyId(Integer propertyId) {
        return analyticsRepository.findMonthlyVariationByPropertyId(propertyId).stream()
                .map(p -> new MonthlyVariationIO(
                        p.getPropertyId(),
                        p.getPropertyName(),
                        p.getReferenceMonth(),
                        p.getTotalLiters(),
                        p.getPreviousMonthLiters(),
                        p.getVariationPct()
                ))
                .toList();
    }

    public List<ConsumptionDistributionIO> findConsumptionDistribution() {
        return analyticsRepository.findConsumptionDistribution().stream()
                .map(p -> new ConsumptionDistributionIO(
                        p.getPropertyKey(),
                        p.getTotalLiters(),
                        p.getDistributionBucket(),
                        p.getMinLiters(),
                        p.getQ1Liters(),
                        p.getMedianLiters(),
                        p.getQ3Liters(),
                        p.getMaxLiters()
                ))
                .toList();
    }

    public List<CapexComparisonIO> findCapexComparison() {
        return analyticsRepository.findCapexComparison().stream()
                .map(p -> new CapexComparisonIO(
                        p.getScenarioId(),
                        p.getName(),
                        p.getInvestmentValue(),
                        p.getReductionPct(),
                        p.getAnnualSavingsValue(),
                        p.getPaybackMonths(),
                        p.getRankByPayback()
                ))
                .toList();
    }

    public List<ResidentialEfficiencyRankingIO> findResidentialEfficiencyRankingByUserId(Integer userId) {
        return analyticsRepository.findResidentialEfficiencyRankingByUserId(userId).stream()
                .map(p -> new ResidentialEfficiencyRankingIO(
                        p.getUserId(),
                        p.getPersonName(),
                        p.getReferenceMonth(),
                        p.getM3Value(),
                        p.getTotalValue(),
                        p.getRankEfficiency(),
                        p.getConsumptionPercentRank()
                ))
                .toList();
    }

    public List<AuditHistoryChainIO> findAuditHistory(Integer regionRateId) {
        return analyticsRepository.findAuditHistory(regionRateId).stream()
                .map(p -> new AuditHistoryChainIO(
                        p.getLogId(),
                        p.getRegionRateId(),
                        p.getM3Value(),
                        p.getOperation(),
                        p.getExecutedBy(),
                        p.getExecutedAt(),
                        p.getLevel()
                ))
                .toList();
    }
}
