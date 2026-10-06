package br.com.delta.delta_api_postgres.modules.device.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceAlreadyExistsException;
import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.auth.security.CurrentUser;
import br.com.delta.delta_api_postgres.modules.device.config.DeviceCredentialProperties;
import br.com.delta.delta_api_postgres.modules.device.dto.io.IssuedDeviceCredentialIO;
import br.com.delta.delta_api_postgres.modules.device.entity.DeviceCredential;
import br.com.delta.delta_api_postgres.modules.device.entity.Device;
import br.com.delta.delta_api_postgres.modules.device.repository.DeviceCredentialRepository;
import br.com.delta.delta_api_postgres.modules.device.repository.DeviceRepository;
import br.com.delta.delta_api_postgres.modules.device.security.DeviceApiKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class DeviceCredentialService {
    private final DeviceRepository devices;
    private final DeviceCredentialRepository credentials;
    private final DeviceApiKeys keys;
    private final DeviceCredentialProperties properties;
    private final CurrentUser currentUser;
    private final Clock clock;
    private final ApplicationEventPublisher events;

    @Transactional
    public IssuedDeviceCredentialIO issue(Integer deviceId) {
        Integer actor = currentUser.id();
        var device = devices.findLockedById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado"));
        if (!device.isActive()) throw new ResourceAlreadyExistsException("Dispositivo inativo.");

        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        credentials.findByCurrentDeviceId(deviceId).ifPresent(previous -> {
            if (previous.isValidAt(now)) {
                throw new ResourceAlreadyExistsException("O dispositivo já possui uma credencial válida.");
            }
            previous.revoke(now);
            credentials.flush();
        });

        return createCredential(device, actor, now, null);
    }

    @Transactional
    public IssuedDeviceCredentialIO rotate(Integer deviceId) {
        Integer actor = currentUser.id();
        var device = devices.findLockedById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado"));
        if (!device.isActive()) throw new ResourceAlreadyExistsException("Dispositivo inativo.");
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        var previous = credentials.findByCurrentDeviceId(deviceId)
                .filter(credential -> credential.isValidAt(now))
                .orElseThrow(() -> new ResourceAlreadyExistsException("Não há credencial válida para trocar. Emita uma credencial primeiro."));
        previous.revoke(now);
        credentials.flush();
        return createCredential(device, actor, now, previous.getId());
    }

    @Transactional
    public void revoke(Integer deviceId) {
        Integer actor = currentUser.id();
        var device = devices.findLockedById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado"));
        credentials.findByCurrentDeviceId(deviceId).ifPresent(credential -> {
            Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
            credential.revoke(now);
            credentials.flush();
            events.publishEvent(new DeviceCredentialAudit.Revoked(deviceId, device.getDeviceId(), credential.getId(), actor, now));
        });
    }

    private IssuedDeviceCredentialIO createCredential(Device device, Integer actor, Instant now, Integer previousId) {
        String key = keys.generate();
        Instant expiresAt = properties.getTtl().isZero() ? null : now.plus(properties.getTtl());
        var credential = credentials.saveAndFlush(new DeviceCredential(device, keys.hash(key), now, expiresAt));
        if (previousId == null) {
            events.publishEvent(new DeviceCredentialAudit.Issued(device.getId(), device.getDeviceId(), credential.getId(), actor, now));
        } else {
            events.publishEvent(new DeviceCredentialAudit.Rotated(device.getId(), device.getDeviceId(), previousId, credential.getId(), actor, now));
        }
        return new IssuedDeviceCredentialIO(credential.getId(), device.getDeviceId(), key, now, expiresAt);
    }
}
