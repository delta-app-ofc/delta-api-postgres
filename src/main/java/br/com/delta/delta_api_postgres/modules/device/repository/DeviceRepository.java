package br.com.delta.delta_api_postgres.modules.device.repository;

import br.com.delta.delta_api_postgres.modules.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Integer> {
    boolean existsByDeviceId(String deviceId);

    boolean existsByDeviceIdAndIdNot(String deviceId, Integer id);
}
