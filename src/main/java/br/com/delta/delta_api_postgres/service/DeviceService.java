package br.com.delta.delta_api_postgres.service;

import br.com.delta.delta_api_postgres.dto.io.DeviceIO;
import br.com.delta.delta_api_postgres.entity.Device;
import br.com.delta.delta_api_postgres.entity.Property;
import br.com.delta.delta_api_postgres.repository.DeviceRepository;
import br.com.delta.delta_api_postgres.repository.PropertyRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final PropertyRepository propertyRepository;


    public DeviceIO create(DeviceIO io) {

        if (deviceRepository.existsByDeviceId(io.deviceId())) {
            throw new IllegalArgumentException("Já existe um dispositivo com esse Device ID.");
        }

        Property property = propertyRepository.findById(io.propertyId())
                .orElseThrow(() -> new EntityNotFoundException("Propriedade não encontrada."));

        Device device = new Device();
        device.setDeviceId(io.deviceId());
        device.setProperty(property);
        device.setIsActive(io.isActive() != null ? io.isActive() : true);
        device.setInstallationDate(LocalDate.now());

        return toIO(deviceRepository.save(device));
    }
}
