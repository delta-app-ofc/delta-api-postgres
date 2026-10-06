package br.com.delta.delta_api_postgres.modules.device.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.Instant;

@Entity
@Table(name = "tb_device_credential",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_device_credential_hash", columnNames = "key_hash"),
                @UniqueConstraint(name = "uq_device_credential_current", columnNames = "current_device_id")
        }, indexes = @Index(name = "idx_device_credential_device", columnList = "device_id"))
@Check(constraints = "((revoked_at is null and current_device_id is not null and current_device_id = device_id) "
        + "or (revoked_at is not null and current_device_id is null)) "
        + "and char_length(key_hash) = 64 "
        + "and (expires_at is null or expires_at > created_at) "
        + "and (revoked_at is null or revoked_at >= created_at)")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class DeviceCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relational reference to tb_device.id, not its canonical device_id string.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_device_credential_device"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Device device;

    @JsonIgnore
    @Column(name = "key_hash", nullable = false, length = 64, updatable = false)
    private String keyHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "expires_at", updatable = false)
    private Instant expiresAt;

    // Unique while unrevoked, NULL afterwards; expiry is checked explicitly.
    @JsonIgnore
    @Column(name = "current_device_id")
    private Integer currentDeviceId;

    public DeviceCredential(Device device, String keyHash, Instant createdAt, Instant expiresAt) {
        if (device == null || device.getId() == null || createdAt == null
                || keyHash == null || !keyHash.matches("[0-9a-f]{64}")
                || (expiresAt != null && !expiresAt.isAfter(createdAt))) {
            throw new IllegalArgumentException("Dados da credencial inválidos.");
        }
        this.device = device;
        this.keyHash = keyHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.currentDeviceId = device.getId();
    }

    public boolean isValidAt(Instant now) {
        return revokedAt == null && (expiresAt == null || expiresAt.isAfter(now));
    }

    public void revoke(Instant now) {
        if (revokedAt != null) return;
        if (now == null || now.isBefore(createdAt)) throw new IllegalArgumentException("Data de revogação inválida.");
        revokedAt = now;
        currentDeviceId = null;
    }
}
