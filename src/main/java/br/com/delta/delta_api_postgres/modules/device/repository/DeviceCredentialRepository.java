package br.com.delta.delta_api_postgres.modules.device.repository;

import br.com.delta.delta_api_postgres.modules.device.entity.DeviceCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeviceCredentialRepository extends JpaRepository<DeviceCredential, Integer> {
    Optional<DeviceCredential> findByKeyHash(String keyHash);
    // May be expired: callers must check isValidAt and retire it before issuing another.
    Optional<DeviceCredential> findByCurrentDeviceId(Integer deviceId);
    List<DeviceCredential> findByDevice_IdOrderByCreatedAtDescIdDesc(Integer deviceId);
}
