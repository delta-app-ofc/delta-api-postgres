package br.com.delta.delta_api_postgres.modules.device.service;

import br.com.delta.delta_api_postgres.modules.device.dto.io.DeviceValidationIO;
import br.com.delta.delta_api_postgres.modules.device.entity.Device;
import br.com.delta.delta_api_postgres.modules.device.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceValidationService {
    private final DeviceRepository devices;

    @Transactional(readOnly = true)
    public DeviceValidationIO validate(String deviceId) {
        return devices.findByDeviceId(deviceId)
                .filter(Device::isActive)
                .map(device -> new DeviceValidationIO(true, device.getDeviceId()))
                .orElseGet(DeviceValidationIO::invalid);
    }
}
