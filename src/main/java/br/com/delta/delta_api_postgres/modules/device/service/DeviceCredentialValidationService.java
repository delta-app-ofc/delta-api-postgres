package br.com.delta.delta_api_postgres.modules.device.service;

import br.com.delta.delta_api_postgres.modules.device.dto.io.DeviceCredentialValidationIO;
import br.com.delta.delta_api_postgres.modules.device.repository.DeviceCredentialRepository;
import br.com.delta.delta_api_postgres.modules.device.security.DeviceApiKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceCredentialValidationService {
    private final DeviceCredentialRepository credentials;
    private final DeviceApiKeys keys;
    private final Clock clock;

    @Transactional(readOnly = true)
    public DeviceCredentialValidationIO validate(String key) {
        if (!key.matches("delta_dev_[A-Za-z0-9_-]{43}")) return DeviceCredentialValidationIO.invalid();
        // Infrastructure failures propagate; they are not invalid credentials.
        return credentials.findByKeyHash(keys.hash(key))
                .filter(credential -> credential.isValidAt(clock.instant()) && credential.getDevice().isActive())
                .map(credential -> new DeviceCredentialValidationIO(true, credential.getDevice().getDeviceId(),
                        credential.getId(), List.of("telemetry:write")))
                .orElseGet(DeviceCredentialValidationIO::invalid);
    }
}
