package br.com.delta.delta_api_postgres.modules.device.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.device.dto.io.DeviceOwnerIO;
import br.com.delta.delta_api_postgres.modules.device.repository.DeviceRepository;
import br.com.delta.delta_api_postgres.modules.property.repository.PropertyOperationDayRepository;
import br.com.delta.delta_api_postgres.modules.property.repository.PropertyShiftRepository;
import br.com.delta.delta_api_postgres.modules.user_property.entity.UserProperty;
import br.com.delta.delta_api_postgres.modules.user_property.repository.UserPropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Responde "de quem é este dispositivo?" para serviços internos, como o detector de anomalias. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceOwnerService {
    // Nomes de tb_day_of_week (com e sem acento) para java.time.DayOfWeek.
    private static final Map<String, String> DAYS = Map.ofEntries(
            Map.entry("SEGUNDA", "MONDAY"),
            Map.entry("TERÇA", "TUESDAY"), Map.entry("TERCA", "TUESDAY"),
            Map.entry("QUARTA", "WEDNESDAY"),
            Map.entry("QUINTA", "THURSDAY"),
            Map.entry("SEXTA", "FRIDAY"),
            Map.entry("SÁBADO", "SATURDAY"), Map.entry("SABADO", "SATURDAY"),
            Map.entry("DOMINGO", "SUNDAY"));

    private final DeviceRepository deviceRepository;
    private final UserPropertyRepository userPropertyRepository;
    private final PropertyShiftRepository shiftRepository;
    private final PropertyOperationDayRepository operationDayRepository;

    public DeviceOwnerIO findByDeviceId(String deviceId) {
        var device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado."));
        var property = device.getProperty();
        var userIds = userPropertyRepository.findByProperty_Id(property.getId()).stream()
                .map(UserProperty::getUserId).sorted().toList();
        List<DeviceOwnerIO.ShiftIO> shifts = shiftRepository.findByPropertyIdOrderByStartTime(property.getId())
                .stream().map(shift -> new DeviceOwnerIO.ShiftIO(shift.getStartTime().toString(),
                        shift.getEndTime().toString())).toList();
        List<String> days = operationDayRepository.findDayNamesByPropertyId(property.getId()).stream()
                .map(name -> DAYS.get(name.trim().toUpperCase(Locale.ROOT).replaceFirst("-FEIRA$", "")))
                .filter(java.util.Objects::nonNull).distinct().sorted().toList();
        return new DeviceOwnerIO(device.getDeviceId(), property.getId(),
                property.getClassification().getGroupName(), userIds, device.isActive(), shifts, days);
    }
}
