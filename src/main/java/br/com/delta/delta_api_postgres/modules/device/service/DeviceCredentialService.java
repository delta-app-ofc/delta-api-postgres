package br.com.delta.delta_api_postgres.modules.device.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceAlreadyExistsException;
import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.auth.security.CurrentUser;
import br.com.delta.delta_api_postgres.modules.device.config.DeviceCredentialProperties;
import br.com.delta.delta_api_postgres.modules.device.dto.io.IssuedDeviceCredentialIO;
import br.com.delta.delta_api_postgres.modules.device.entity.DeviceCredential;
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

        String key = keys.generate();
        Instant expiresAt = properties.getTtl().isZero() ? null : now.plus(properties.getTtl());
        var credential = credentials.saveAndFlush(new DeviceCredential(device, keys.hash(key), now, expiresAt));
        events.publishEvent(new DeviceCredentialAudit.Issued(deviceId, device.getDeviceId(), credential.getId(), actor, now));
        return new IssuedDeviceCredentialIO(credential.getId(), device.getDeviceId(), key, now, expiresAt);
    }
}
