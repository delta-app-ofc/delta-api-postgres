package br.com.delta.delta_api_postgres.modules.analytics.controller;

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
}
