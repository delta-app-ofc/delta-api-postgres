package br.com.delta.delta_api_postgres.modules.device.mapper;

import br.com.delta.delta_api_postgres.modules.device.dto.io.DeviceIO;
import br.com.delta.delta_api_postgres.modules.device.dto.request.CreateDeviceRequest;
import br.com.delta.delta_api_postgres.modules.device.dto.request.UpdateDeviceRequest;
import br.com.delta.delta_api_postgres.modules.device.entity.Device;
import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DeviceMapper {

    public DeviceIO fromCreateRequest(CreateDeviceRequest request) {

        return new DeviceIO(
                null,
                request.deviceId(),
                request.propertyId(),
                request.isActive(),
                null
        );
    }

    public DeviceIO fromUpdateRequest(
            Integer id,
            UpdateDeviceRequest request
    ) {

        return new DeviceIO(
                id,
                request.deviceId(),
                request.propertyId(),
                request.isActive(),
                null
        );
    }


    public DeviceIO toIO(Device device) {

        return new DeviceIO(
                device.getId(),
                device.getDeviceId(),
                device.getProperty().getId(),
                device.isActive(),
                device.getInstallationDate()
        );
    }

    public Device toEntity(
            DeviceIO io,
            Property property
    ) {

        Device device = new Device();

        device.setDeviceId(io.deviceId());
        device.setProperty(property);
        device.setActive(io.isActive());
        device.setInstallationDate(
                io.installationDate() != null
                        ? io.installationDate()
                        : LocalDate.now()
        );

        return device;
    }

    public void updateEntity(
            Device device,
            DeviceIO io,
            Property property
    ) {

        device.setDeviceId(io.deviceId());
        device.setProperty(property);
        device.setActive(io.isActive());

    }
}
