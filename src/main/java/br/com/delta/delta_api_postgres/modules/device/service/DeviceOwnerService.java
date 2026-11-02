package br.com.delta.delta_api_postgres.modules.device.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.device.dto.io.DeviceOwnerIO;
import br.com.delta.delta_api_postgres.modules.device.repository.DeviceRepository;
import br.com.delta.delta_api_postgres.modules.user_property.entity.UserProperty;
import br.com.delta.delta_api_postgres.modules.user_property.repository.UserPropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Responde "de quem é este dispositivo?" para serviços internos, como o detector de anomalias. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceOwnerService {
    private final DeviceRepository deviceRepository;
    private final UserPropertyRepository userPropertyRepository;

    public DeviceOwnerIO findByDeviceId(String deviceId) {
        var device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado."));
        var property = device.getProperty();
        var userIds = userPropertyRepository.findByProperty_Id(property.getId()).stream()
                .map(UserProperty::getUserId).sorted().toList();
        return new DeviceOwnerIO(device.getDeviceId(), property.getId(),
                property.getClassification().getGroupName(), userIds, device.isActive());
    }
}
