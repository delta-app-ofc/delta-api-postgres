package br.com.delta.delta_api_postgres.modules.analytics.controller;

import br.com.delta.delta_api_postgres.modules.analytics.dto.io.AlertIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.OrganizationMonthlyKpiIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.PropertyMonthlyIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.MonitoringHourlyIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.DeviceStatusIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.DeviceSummaryIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.LeakIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.OrganizationEsgIO;
import org.springframework.format.annotation.DateTimeFormat;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.AuditHistoryChainIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.CapexComparisonIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.ConsumptionDailyIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.ConsumptionDistributionIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.MonthlyVariationIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.PropertyRankingIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.ResidentialEfficiencyRankingIO;
import br.com.delta.delta_api_postgres.modules.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/delta/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/consumption-daily/{propertyId}")
    public ResponseEntity<List<ConsumptionDailyIO>> findConsumptionDailyByPropertyId(
            @PathVariable Integer propertyId) {

        return ResponseEntity.ok(
                analyticsService.findConsumptionDailyByPropertyId(propertyId)
        );
    }

    @GetMapping("/property-ranking")
    public ResponseEntity<List<PropertyRankingIO>> findPropertyRanking() {
        return ResponseEntity.ok(
                analyticsService.findPropertyRanking()
        );
    }

    @GetMapping("/monthly-variation/{propertyId}")
    public ResponseEntity<List<MonthlyVariationIO>> findMonthlyVariationByPropertyId(
            @PathVariable Integer propertyId) {

        return ResponseEntity.ok(
                analyticsService.findMonthlyVariationByPropertyId(propertyId)
        );
    }

    @GetMapping("/consumption-distribution")
    public ResponseEntity<List<ConsumptionDistributionIO>> findConsumptionDistribution() {
        return ResponseEntity.ok(
                analyticsService.findConsumptionDistribution()
        );
    }

    @GetMapping("/capex-comparison")
    public ResponseEntity<List<CapexComparisonIO>> findCapexComparison() {
        return ResponseEntity.ok(
                analyticsService.findCapexComparison()
        );
    }

    @GetMapping("/residential-efficiency-ranking/{userId}")
    public ResponseEntity<List<ResidentialEfficiencyRankingIO>> findResidentialEfficiencyRankingByUserId(
            @PathVariable Integer userId) {

        return ResponseEntity.ok(
                analyticsService.findResidentialEfficiencyRankingByUserId(userId)
        );
    }

    @GetMapping("/audit-history")
    public ResponseEntity<List<AuditHistoryChainIO>> findAuditHistory(
            @RequestParam(required = false) Integer regionRateId) {

        return ResponseEntity.ok(
                analyticsService.findAuditHistory(regionRateId)
        );
    }

    @GetMapping("/organization-kpi")
    public ResponseEntity<List<OrganizationMonthlyKpiIO>> findOrganizationMonthlyKpi() {
        return ResponseEntity.ok(analyticsService.findOrganizationMonthlyKpi());
    }

    @GetMapping("/property-monthly")
    public ResponseEntity<List<PropertyMonthlyIO>> findPropertyMonthly(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        return ResponseEntity.ok(analyticsService.findPropertyMonthly(month));
    }

    @GetMapping("/monitoring-hourly")
    public ResponseEntity<List<MonitoringHourlyIO>> findMonitoringHourly(
            @RequestParam(defaultValue = "24") Integer hours,
            @RequestParam(required = false) Integer windowMinutes) {
        return ResponseEntity.ok(analyticsService.findMonitoringHourly(hours, windowMinutes));
    }

    @GetMapping("/devices")
    public ResponseEntity<List<DeviceStatusIO>> findDeviceStatus() {
        return ResponseEntity.ok(analyticsService.findDeviceStatus());
    }

    @GetMapping("/devices/summary")
    public ResponseEntity<List<DeviceSummaryIO>> findDeviceSummary() {
        return ResponseEntity.ok(analyticsService.findDeviceSummary());
    }

    @GetMapping("/leaks")
    public ResponseEntity<List<LeakIO>> findLeaks(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(analyticsService.findLeaks(status));
    }

    @GetMapping("/esg")
    public ResponseEntity<List<OrganizationEsgIO>> findOrganizationEsg() {
        return ResponseEntity.ok(analyticsService.findOrganizationEsg());
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<AlertIO>> findAlerts(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String alertType,
            @RequestParam(required = false) Boolean onlyActive) {
        return ResponseEntity.ok(analyticsService.findAlerts(severity, alertType, onlyActive));
    }
}

