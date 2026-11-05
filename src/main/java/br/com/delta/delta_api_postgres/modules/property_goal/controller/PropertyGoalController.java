package br.com.delta.delta_api_postgres.modules.property_goal.controller;

import br.com.delta.delta_api_postgres.modules.property_goal.dto.io.PropertyGoalIO;
import br.com.delta.delta_api_postgres.modules.property_goal.dto.request.UpdatePropertyGoalRequest;
import br.com.delta.delta_api_postgres.modules.property_goal.service.PropertyGoalService;
import br.com.delta.delta_api_postgres.modules.property_goal.swagger.PropertyGoalSwagger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/delta/property/{propertyId}/goal")
@RequiredArgsConstructor
public class PropertyGoalController implements PropertyGoalSwagger {
    private final PropertyGoalService service;

    @GetMapping
    @Override
    public ResponseEntity<PropertyGoalIO> find(@PathVariable Integer propertyId) {
        return ResponseEntity.ok(service.find(propertyId));
    }

    @PutMapping
    @Override
    public ResponseEntity<PropertyGoalIO> save(@PathVariable Integer propertyId,
                                               @Valid @RequestBody UpdatePropertyGoalRequest request) {
        return ResponseEntity.ok(service.save(propertyId, request.monthlyTargetLiters()));
    }
}
