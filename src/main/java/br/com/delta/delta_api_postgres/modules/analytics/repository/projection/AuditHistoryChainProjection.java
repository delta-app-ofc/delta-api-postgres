package br.com.delta.delta_api_postgres.modules.analytics.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface AuditHistoryChainProjection {
    Integer getLogId();

    Integer getRegionRateId();

    BigDecimal getM3Value();

    String getOperation();

    String getExecutedBy();

    LocalDateTime getExecutedAt();

    Integer getLevel();
}
