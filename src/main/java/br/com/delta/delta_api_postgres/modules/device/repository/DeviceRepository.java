package br.com.delta.delta_api_postgres.modules.device.repository;

import br.com.delta.delta_api_postgres.modules.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Integer> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select d from Device d where d.id = :id")
    Optional<Device> findLockedById(@org.springframework.data.repository.query.Param("id") Integer id);

    Optional<Device> findByDeviceId(String deviceId);

    boolean existsByDeviceId(String deviceId);

    boolean existsByDeviceIdAndIdNot(String deviceId, Integer id);
}
