package br.com.delta.delta_api_postgres.modules.analytics.dto.io;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlertIO(
        Long alertKey,
        Integer propertyId,
        String propertyName,
        String deviceId,
        String alertType,
        String severity,
        LocalDateTime triggeredAt,
        LocalDateTime resolvedAt,
        BigDecimal resolutionMinutes
) {
}

