package br.com.delta.delta_api_postgres.modules.device.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import br.com.delta.delta_api_postgres.modules.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Integer> {
    boolean existsByProperty_Id(Integer propertyId);
    @Query("select d from Device d where exists (select u.id from UserProperty u where u.property = d.property and u.userId = :userId)")
    List<Device> findForUser(@Param("userId") Integer userId);

    Optional<Device> findByDeviceId(String deviceId);

    boolean existsByDeviceId(String deviceId);

    boolean existsByDeviceIdAndIdNot(String deviceId, Integer id);
}
