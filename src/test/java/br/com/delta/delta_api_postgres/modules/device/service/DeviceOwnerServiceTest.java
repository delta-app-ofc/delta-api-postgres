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
import br.com.delta.delta_api_postgres.modules.device.dto.io.DeviceOwnerIO;
import br.com.delta.delta_api_postgres.modules.property.entity.PropertyShift;
import br.com.delta.delta_api_postgres.modules.property.repository.PropertyOperationDayRepository;
import br.com.delta.delta_api_postgres.modules.property.repository.PropertyShiftRepository;
import java.time.LocalDate;
import java.time.LocalTime;
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
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceOwnerServiceTest {
    private static final String DEVICE_CODE = "DEVICE-001";
    private static final Integer PROPERTY_ID = 10;

    @Mock private DeviceRepository deviceRepository;
    @Mock private UserPropertyRepository userPropertyRepository;
    @Mock private PropertyShiftRepository shiftRepository;
    @Mock private PropertyOperationDayRepository operationDayRepository;
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
    void returnsTheBusinessShiftsAndTheOperationDaysOfACompany() {
        when(deviceRepository.findByDeviceId(DEVICE_CODE))
                .thenReturn(Optional.of(device(PropertyClassificationGroup.COMERCIAL, true)));
        when(userPropertyRepository.findByProperty_Id(PROPERTY_ID)).thenReturn(List.of(link(50)));
        when(shiftRepository.findByPropertyIdOrderByStartTime(PROPERTY_ID)).thenReturn(List.of(
                shift(LocalTime.of(8, 0), LocalTime.of(12, 0)), shift(LocalTime.of(14, 0), LocalTime.of(18, 30))));
        when(operationDayRepository.findDayNamesByPropertyId(PROPERTY_ID))
                .thenReturn(List.of("SEXTA", "SEGUNDA", "TERÇA", "SÁBADO", "SEGUNDA"));

        var owner = service.findByDeviceId(DEVICE_CODE);

        assertThat(owner.shifts()).extracting(DeviceOwnerIO.ShiftIO::startTime, DeviceOwnerIO.ShiftIO::endTime)
                .containsExactly(tuple("08:00", "12:00"), tuple("14:00", "18:30"));
        assertThat(owner.operationDays()).containsExactly("FRIDAY", "MONDAY", "SATURDAY", "TUESDAY");
    }

    @Test
    void ignoresUnknownDayNamesAndAcceptsNamesWithoutAccents() {
        when(deviceRepository.findByDeviceId(DEVICE_CODE))
                .thenReturn(Optional.of(device(PropertyClassificationGroup.COMERCIAL, true)));
        when(userPropertyRepository.findByProperty_Id(PROPERTY_ID)).thenReturn(List.of());
        when(operationDayRepository.findDayNamesByPropertyId(PROPERTY_ID))
                .thenReturn(List.of("terca", "FERIADO", " domingo "));

        var owner = service.findByDeviceId(DEVICE_CODE);

        assertThat(owner.operationDays()).containsExactly("SUNDAY", "TUESDAY");
        assertThat(owner.shifts()).isEmpty();
    }

    @Test
    void acceptsTheFeiraSpellingOfTheDayNames() {
        when(deviceRepository.findByDeviceId(DEVICE_CODE))
                .thenReturn(Optional.of(device(PropertyClassificationGroup.COMERCIAL, true)));
        when(userPropertyRepository.findByProperty_Id(PROPERTY_ID)).thenReturn(List.of());
        when(operationDayRepository.findDayNamesByPropertyId(PROPERTY_ID))
                .thenReturn(List.of("Segunda-feira", "Terça-feira", "quarta-feira", "Quinta-Feira", "Sexta-feira",
                        "Sábado", "Domingo"));

        var owner = service.findByDeviceId(DEVICE_CODE);

        assertThat(owner.operationDays()).containsExactly("FRIDAY", "MONDAY", "SATURDAY", "SUNDAY", "THURSDAY",
                "TUESDAY", "WEDNESDAY");
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

    private static PropertyShift shift(LocalTime start, LocalTime end) {
        var shift = new PropertyShift();
        shift.setStartTime(start);
        shift.setEndTime(end);
        return shift;
    }

    private static UserProperty link(Integer userId) {
        var link = new UserProperty();
        ReflectionTestUtils.setField(link, "userId", userId);
        return link;
    }
}
