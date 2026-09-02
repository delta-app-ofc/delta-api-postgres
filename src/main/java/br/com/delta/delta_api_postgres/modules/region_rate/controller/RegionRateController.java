package br.com.delta.delta_api_postgres.modules.region_rate.controller;

import br.com.delta.delta_api_postgres.modules.region_rate.dto.io.RegionRateIO;
import br.com.delta.delta_api_postgres.modules.region_rate.service.RegionRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/delta/region-rate")
@RequiredArgsConstructor
public class RegionRateController {

    private final RegionRateService regionRateService;

    @GetMapping("/{regionId}")
    public ResponseEntity<List<RegionRateIO>> findByRegion(
            @PathVariable Integer regionId,
            @RequestParam(required = false) Boolean last) {

        return ResponseEntity.ok(
                regionRateService.findByRegion(regionId, last)
        );
    }
}
