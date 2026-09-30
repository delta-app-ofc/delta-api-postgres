package br.com.delta.delta_api_postgres.modules.analytics.dto.io;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuditHistoryChainIO(
        Integer logId,
        Integer regionRateId,
        BigDecimal m3Value,
        String operation,
        String executedBy,
        LocalDateTime executedAt,
        Integer level
) {
}
