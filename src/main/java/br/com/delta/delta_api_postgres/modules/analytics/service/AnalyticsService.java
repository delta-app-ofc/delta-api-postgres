package br.com.delta.delta_api_postgres.modules.analytics.service;

import br.com.delta.delta_api_postgres.modules.analytics.dto.io.AlertIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.OrganizationMonthlyKpiIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.PropertyMonthlyIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.MonitoringHourlyIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.DeviceStatusIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.DeviceSummaryIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.LeakIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.OrganizationEsgIO;
import java.time.LocalDate;
import br.com.delta.delta_api_postgres.modules.auth.security.UserDataAccess;

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
    private final UserDataAccess access;

    private final AnalyticsRepository analyticsRepository;

    public List<ConsumptionDailyIO> findConsumptionDailyByPropertyId(Integer propertyId) {
        access.property(propertyId);
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
        return analyticsRepository.findPropertyRanking(access.userId()).stream()
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
        access.property(propertyId);
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
        access.denyUnscopedAnalytics();
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
        access.requireUser(userId);
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
        access.denyUnscopedAnalytics();
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

    public List<OrganizationMonthlyKpiIO> findOrganizationMonthlyKpi() {
        return analyticsRepository.findOrganizationMonthlyKpi(access.userId()).stream()
                .map(p -> new OrganizationMonthlyKpiIO(
                        p.getOrganizationId(),
                        p.getReferenceMonth(),
                        p.getTotalLiters(),
                        p.getTotalCost(),
                        p.getPreviousMonthLiters(),
                        p.getVariationPct(),
                        p.getProjectedLiters(),
                        p.getProjectedCost(),
                        p.getUnitsTotal(),
                        p.getUnitsOnTarget(),
                        p.getUnitsAttention(),
                        p.getUnitsCritical(),
                        p.getAvgGoalPct(),
                        p.getEstimatedSavingsValue(),
                        p.getSavedLiters(),
                        p.getMinDaysWithData(),
                        p.getIsCompleteMonth()
                ))
                .toList();
    }

    public List<PropertyMonthlyIO> findPropertyMonthly(LocalDate month) {
        return analyticsRepository.findPropertyMonthly(access.userId(), month).stream()
                .map(p -> new PropertyMonthlyIO(
                        p.getPropertyId(),
                        p.getPropertyName(),
                        p.getOrganizationId(),
                        p.getCity(),
                        p.getState(),
                        p.getRegionName(),
                        p.getLatitude(),
                        p.getLongitude(),
                        p.getBuiltAreaM2(),
                        p.getReferenceMonth(),
                        p.getDaysWithData(),
                        p.getIsCompleteMonth(),
                        p.getTotalLiters(),
                        p.getTotalCost(),
                        p.getLitersPerM2(),
                        p.getPreviousMonthLiters(),
                        p.getVariationPct(),
                        p.getMonthlyTargetLiters(),
                        p.getGoalPct(),
                        p.getGoalStatus(),
                        p.getEstimatedSavingsValue(),
                        p.getSavedLiters()
                ))
                .toList();
    }

    public List<MonitoringHourlyIO> findMonitoringHourly(Integer hours, Integer windowMinutes) {
        return analyticsRepository.findMonitoringHourly(access.userId(), hours, windowMinutes).stream()
                .map(p -> new MonitoringHourlyIO(
                        p.getOrganizationId(),
                        p.getPropertyId(),
                        p.getPropertyName(),
                        p.getWindowMinutes(),
                        p.getReadingHour(),
                        p.getTotalLiters(),
                        p.getAvgFlowLmin(),
                        p.getReadingsCount()
                ))
                .toList();
    }

    public List<DeviceStatusIO> findDeviceStatus() {
        return analyticsRepository.findDeviceStatus(access.userId()).stream()
                .map(p -> new DeviceStatusIO(
                        p.getOrganizationId(),
                        p.getPropertyId(),
                        p.getPropertyName(),
                        p.getRegionName(),
                        p.getDeviceId(),
                        p.getIsActive(),
                        p.getConnectivityStatus(),
                        p.getIsOnline(),
                        p.getBatteryLevel(),
                        p.getIsLowBattery(),
                        p.getWifiSignalRssi(),
                        p.getFirmwareVersion(),
                        p.getLastPingAt(),
                        p.getUnavailabilityReason()
                ))
                .toList();
    }

    public List<DeviceSummaryIO> findDeviceSummary() {
        return analyticsRepository.findDeviceSummary(access.userId()).stream()
                .map(p -> new DeviceSummaryIO(
                        p.getOrganizationId(),
                        p.getPropertiesTotal(),
                        p.getDevicesPaired(),
                        p.getDevicesOnline(),
                        p.getDevicesLowBattery(),
                        p.getPropertiesWithoutDevice()
                ))
                .toList();
    }

    public List<LeakIO> findLeaks(String status) {
        return analyticsRepository.findLeaks(access.userId(), status).stream()
                .map(p -> new LeakIO(
                        p.getAlertKey(),
                        p.getOrganizationId(),
                        p.getPropertyId(),
                        p.getPropertyName(),
                        p.getDeviceId(),
                        p.getSeverity(),
                        p.getTriggeredAt(),
                        p.getResolvedAt(),
                        p.getLeakStatus(),
                        p.getDurationMinutes(),
                        p.getEstimatedLossLiters(),
                        p.getEstimatedLossCost()
                ))
                .toList();
    }

    public List<OrganizationEsgIO> findOrganizationEsg() {
        return analyticsRepository.findOrganizationEsg(access.userId()).stream()
                .map(p -> new OrganizationEsgIO(
                        p.getOrganizationId(),
                        p.getReferenceMonth(),
                        p.getSavedLitersMonth(),
                        p.getSavedLitersYearProjection(),
                        p.getTankTrucksEquivalent(),
                        p.getFamilyMonthsEquivalent(),
                        p.getUnitsTotal(),
                        p.getUnitsOnTarget(),
                        p.getAvgGoalPct(),
                        p.getEstimatedSavingsValue(),
                        p.getIsCompleteMonth()
                ))
                .toList();
    }

    public List<AlertIO> findAlerts(String severity, String alertType, Boolean onlyActive) {
        return analyticsRepository.findAlerts(access.userId(), severity, alertType, onlyActive).stream()
                .map(p -> new AlertIO(
                        p.getAlertKey(),
                        p.getPropertyId(),
                        p.getPropertyName(),
                        p.getDeviceId(),
                        p.getAlertType(),
                        p.getSeverity(),
                        p.getTriggeredAt(),
                        p.getResolvedAt(),
                        p.getResolutionMinutes()
                ))
                .toList();
    }
}

