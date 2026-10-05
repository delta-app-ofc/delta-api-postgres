package br.com.delta.delta_api_postgres.modules.device.mapper;

import br.com.delta.delta_api_postgres.modules.device.dto.io.DeviceIO;
import br.com.delta.delta_api_postgres.modules.device.dto.request.CreateDeviceRequest;
import br.com.delta.delta_api_postgres.modules.device.entity.Device;
import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

class DeviceMapperTest {
    private final DeviceMapper mapper = new DeviceMapper();

    @Test void createAndReadPreserveCanonicalStringAndSeparateSqlId() {
        Property property = new Property();
        property.setId(10);
        Device entity = mapper.toEntity(mapper.fromCreateRequest(
                new CreateDeviceRequest("001", 10, true)), property);
        entity.setId(42);

        DeviceIO result = mapper.toIO(entity);

        assertThat(result.id()).isEqualTo(42);
        assertThat(result.deviceId()).isEqualTo("001");
    }

    @Test void updatePreservesIdentityWhileChangingPropertyAndStatus() {
        Device entity = new Device(42, "DEVICE-001", new Property(), true, LocalDate.of(2026, 1, 1));
        Property target = new Property();
        target.setId(20);

        mapper.updateEntity(entity, new DeviceIO(42, "DIFFERENT", 20, false, null), target);

        assertThat(entity.getDeviceId()).isEqualTo("DEVICE-001");
        assertThat(entity.getProperty()).isSameAs(target);
        assertThat(entity.isActive()).isFalse();
        assertThat(entity.getInstallationDate()).isEqualTo(LocalDate.of(2026, 1, 1));
    }
}
