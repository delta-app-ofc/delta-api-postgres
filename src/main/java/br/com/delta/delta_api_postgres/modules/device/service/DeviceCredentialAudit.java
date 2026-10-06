package br.com.delta.delta_api_postgres.modules.device.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import java.time.Instant;

@Component
@Slf4j
public class DeviceCredentialAudit {
    public record Issued(Integer deviceSqlId, String deviceId, Integer credentialId, Integer actorUserId, Instant occurredAt) {}
    public record Rotated(Integer deviceSqlId, String deviceId, Integer previousCredentialId,
                          Integer credentialId, Integer actorUserId, Instant occurredAt) {}
    public record Revoked(Integer deviceSqlId, String deviceId, Integer credentialId, Integer actorUserId, Instant occurredAt) {}

    @TransactionalEventListener
    public void recordRevocation(Revoked event) {
        log.info("device_credential_revoked device_sql_id={} device_id={} credential_id={} actor_user_id={} occurred_at={}",
                event.deviceSqlId(), event.deviceId(), event.credentialId(), event.actorUserId(), event.occurredAt());
    }

    @TransactionalEventListener
    public void recordRotation(Rotated event) {
        log.info("device_credential_rotated device_sql_id={} device_id={} previous_credential_id={} credential_id={} actor_user_id={} occurred_at={}",
                event.deviceSqlId(), event.deviceId(), event.previousCredentialId(), event.credentialId(), event.actorUserId(), event.occurredAt());
    }

    @TransactionalEventListener
    public void recordIssuance(Issued event) {
        log.info("device_credential_issued device_sql_id={} device_id={} credential_id={} actor_user_id={} occurred_at={}",
                event.deviceSqlId(), event.deviceId(), event.credentialId(), event.actorUserId(), event.occurredAt());
    }
}
