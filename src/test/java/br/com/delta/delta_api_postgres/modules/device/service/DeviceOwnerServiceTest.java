package br.com.delta.delta_api_postgres.modules.device.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.device.entity.Device;
import br.com.delta.delta_api_postgres.modules.device.repository.DeviceRepository;
import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import br.com.delta.delta_api_postgres.modules.property.entity.PropertyClassificationEntity;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyClassification;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyClassificationGroup;
import br.com.delta.delta_api_postgres.modules.user_property.entity.UserProperty;
import br.com.delta.delta_api_postgres.modules.user_property.repository.UserPropertyRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceOwnerServiceTest {
    private static final String DEVICE_CODE = "DEVICE-001";
    private static final Integer PROPERTY_ID = 10;

    @Mock private DeviceRepository deviceRepository;
    @Mock private UserPropertyRepository userPropertyRepository;
    @InjectMocks private DeviceOwnerService service;

    @Test
    void returnsPropertyClassificationAndAllUsersOfTheProperty() {
        when(deviceRepository.findByDeviceId(DEVICE_CODE))
                .thenReturn(Optional.of(device(PropertyClassificationGroup.RESIDENCIAL, true)));
        when(userPropertyRepository.findByProperty_Id(PROPERTY_ID))
                .thenReturn(List.of(link(30), link(7)));

        var owner = service.findByDeviceId(DEVICE_CODE);

        assertThat(owner.deviceId()).isEqualTo(DEVICE_CODE);
        assertThat(owner.propertyId()).isEqualTo(PROPERTY_ID);
        assertThat(owner.classificationGroup()).isEqualTo(PropertyClassificationGroup.RESIDENCIAL);
        assertThat(owner.userIds()).containsExactly(7, 30);
        assertThat(owner.isActive()).isTrue();
    }

    @Test
    void reportsACommercialPropertyAndAnInactiveDevice() {
        when(deviceRepository.findByDeviceId(DEVICE_CODE))
                .thenReturn(Optional.of(device(PropertyClassificationGroup.COMERCIAL, false)));
        when(userPropertyRepository.findByProperty_Id(PROPERTY_ID)).thenReturn(List.of());

        var owner = service.findByDeviceId(DEVICE_CODE);

        assertThat(owner.classificationGroup()).isEqualTo(PropertyClassificationGroup.COMERCIAL);
        assertThat(owner.userIds()).isEmpty();
        assertThat(owner.isActive()).isFalse();
    }

    @Test
    void failsWithNotFoundWhenTheDeviceDoesNotExist() {
        when(deviceRepository.findByDeviceId("DESCONHECIDO")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByDeviceId("DESCONHECIDO"))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(userPropertyRepository);
    }

    private static Device device(PropertyClassificationGroup group, boolean active) {
        var classification = new PropertyClassificationEntity(1, PropertyClassification.values()[0], group);
        var property = new Property();
        property.setId(PROPERTY_ID);
        property.setClassification(classification);
        var device = new Device();
        device.setDeviceId(DEVICE_CODE);
        device.setProperty(property);
        device.setActive(active);
        device.setInstallationDate(LocalDate.of(2026, 1, 15));
        return device;
    }

    private static UserProperty link(Integer userId) {
        var link = new UserProperty();
        ReflectionTestUtils.setField(link, "userId", userId);
        return link;
    }
}
