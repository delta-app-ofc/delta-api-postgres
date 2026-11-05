package br.com.delta.delta_api_postgres.modules.property_goal.dto.io;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PropertyGoalIO(
        Integer propertyId,
        BigDecimal monthlyTargetLiters,
        LocalDateTime updatedAt
) {
}
