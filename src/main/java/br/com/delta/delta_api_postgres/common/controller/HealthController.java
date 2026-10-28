package br.com.delta.delta_api_postgres.common.controller;

import br.com.delta.delta_api_postgres.common.service.HealthService;
import br.com.delta.delta_api_postgres.common.swagger.HealthSwagger;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController implements HealthSwagger {
    private final HealthService healthService;

    public record HealthResponse(@Schema(allowableValues = {"UP", "DOWN"}) String status) {}

    @Override
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        if (healthService.isHealthy()) {
            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new HealthResponse("UP"));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .cacheControl(CacheControl.noStore()).body(new HealthResponse("DOWN"));
    }
}
