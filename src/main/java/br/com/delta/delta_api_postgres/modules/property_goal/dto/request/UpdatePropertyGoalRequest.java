package br.com.delta.delta_api_postgres.modules.property_goal.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdatePropertyGoalRequest(
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @DecimalMax("999999999.99")
        BigDecimal monthlyTargetLiters
) {
}
