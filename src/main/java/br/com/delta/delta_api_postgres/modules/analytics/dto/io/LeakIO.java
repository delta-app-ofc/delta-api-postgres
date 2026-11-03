package br.com.delta.delta_api_postgres.modules.analytics.dto.io;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LeakIO(
        Long alertKey,
        Integer organizationId,
        Integer propertyId,
        String propertyName,
        String deviceId,
        String severity,
        LocalDateTime triggeredAt,
        LocalDateTime resolvedAt,
        String leakStatus,
        BigDecimal durationMinutes,
        BigDecimal estimatedLossLiters,
        BigDecimal estimatedLossCost
) {
}

