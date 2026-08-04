package br.com.delta.delta_api_postgres.mapper;

import br.com.delta.delta_api_postgres.dto.io.DeviceIO;
import br.com.delta.delta_api_postgres.entity.Device;
import br.com.delta.delta_api_postgres.entity.Property;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DeviceMapper {


    public DeviceIO toIO(Device device) {

        return new DeviceIO(
                device.getId(),
                device.getDeviceId(),
                device.getProperty().getId(),
                device.getIsActive(),
                device.getInstallationDate()
        );
    }


    public Device toEntity(DeviceIO io, Property property) {

        Device device = new Device();

        device.setDeviceId(io.deviceId());
        device.setProperty(property);
        device.setIsActive(io.isActive());
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
        device.setIsActive(io.isActive());

    }
}
