package br.com.delta.delta_api_postgres.modules.device.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceAlreadyExistsException;
import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.device.dto.io.DeviceIO;
import br.com.delta.delta_api_postgres.modules.device.entity.Device;
import br.com.delta.delta_api_postgres.modules.device.mapper.DeviceMapper;
import br.com.delta.delta_api_postgres.modules.device.repository.DeviceRepository;
import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import br.com.delta.delta_api_postgres.modules.property.repository.PropertyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    private static final Integer DEVICE_ID = 1;
    private static final Integer PROPERTY_ID = 10;
    private static final String DEVICE_CODE = "DEVICE-001";
    private static final LocalDate INSTALLATION_DATE = LocalDate.of(2026, 1, 15);

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private DeviceMapper deviceMapper;

    @InjectMocks
    private DeviceService deviceService;

    @Test
    void create_quandoDadosValidos_deveSalvarERetornarDispositivo() {
        // Arrange
        DeviceIO input = inputDeviceIO();
        Property property = property();
        Device device = device(null, property);
        Device savedDevice = device(DEVICE_ID, property);
        DeviceIO expected = savedDeviceIO();

        when(deviceRepository.existsByDeviceId(DEVICE_CODE)).thenReturn(false);
        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property));
        when(deviceMapper.toEntity(input, property)).thenReturn(device);
        when(deviceRepository.save(device)).thenReturn(savedDevice);
        when(deviceMapper.toIO(savedDevice)).thenReturn(expected);

        // Act
        DeviceIO result = deviceService.create(input);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(deviceRepository).save(device);
        verify(deviceMapper).toIO(savedDevice);
    }

    @Test
    void create_quandoDeviceIdJaExiste_deveLancarResourceAlreadyExistsException() {
        DeviceIO input = inputDeviceIO();
        when(deviceRepository.existsByDeviceId(DEVICE_CODE)).thenReturn(true);

        assertThatThrownBy(() -> deviceService.create(input))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Dispositivo já cadastrado");

        verify(deviceRepository, never()).save(any());
        verifyNoInteractions(propertyRepository, deviceMapper);
    }

    @Test
    void create_quandoPropriedadeNaoExiste_deveLancarResourceNotFoundException() {
        DeviceIO input = inputDeviceIO();
        when(deviceRepository.existsByDeviceId(DEVICE_CODE)).thenReturn(false);
        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.create(input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Propriedade não encontrada");

        verify(deviceRepository, never()).save(any());
        verifyNoInteractions(deviceMapper);
    }

    @Test
    void findAll_quandoExistemDispositivos_deveRetornarTodosMapeados() {
        Property property = property();
        Device firstDevice = device(DEVICE_ID, property);
        Device secondDevice = device(2, property);
        secondDevice.setDeviceId("DEVICE-002");

        DeviceIO firstIO = savedDeviceIO();
        DeviceIO secondIO = new DeviceIO(2, "DEVICE-002", PROPERTY_ID, true, INSTALLATION_DATE);

        when(deviceRepository.findAll()).thenReturn(List.of(firstDevice, secondDevice));
        when(deviceMapper.toIO(firstDevice)).thenReturn(firstIO);
        when(deviceMapper.toIO(secondDevice)).thenReturn(secondIO);

        List<DeviceIO> result = deviceService.findAll();

        assertThat(result).containsExactly(firstIO, secondIO);
        verify(deviceMapper).toIO(firstDevice);
        verify(deviceMapper).toIO(secondDevice);
    }

    @Test
    void findAll_quandoNaoExistemDispositivos_deveRetornarListaVazia() {
        when(deviceRepository.findAll()).thenReturn(List.of());

        List<DeviceIO> result = deviceService.findAll();

        assertThat(result).isEmpty();
        verifyNoInteractions(deviceMapper);
    }

    @Test
    void findById_quandoDispositivoExiste_deveRetornarDispositivoMapeado() {
        Device device = device(DEVICE_ID, property());
        DeviceIO expected = savedDeviceIO();

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));
        when(deviceMapper.toIO(device)).thenReturn(expected);

        DeviceIO result = deviceService.findById(DEVICE_ID);

        assertThat(result).isEqualTo(expected);
        verify(deviceMapper).toIO(device);
    }

    @Test
    void findById_quandoDispositivoNaoExiste_deveLancarResourceNotFoundException() {
        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.findById(DEVICE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Dispositivo não encontrado");

        verifyNoInteractions(deviceMapper);
    }

    @Test
    void update_quandoDadosValidos_deveAtualizarERetornarDispositivo() {
        DeviceIO input = inputDeviceIO();
        Property property = property();
        Device device = device(DEVICE_ID, property);
        DeviceIO expected = savedDeviceIO();

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));
        when(deviceRepository.existsByDeviceIdAndIdNot(DEVICE_CODE, DEVICE_ID)).thenReturn(false);
        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property));
        when(deviceRepository.save(device)).thenReturn(device);
        when(deviceMapper.toIO(device)).thenReturn(expected);

        DeviceIO result = deviceService.update(DEVICE_ID, input);

        assertThat(result).isEqualTo(expected);
        verify(deviceMapper).updateEntity(device, input, property);
        verify(deviceRepository).save(device);
    }

    @Test
    void update_quandoDispositivoNaoExiste_deveLancarResourceNotFoundException() {
        DeviceIO input = inputDeviceIO();
        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.update(DEVICE_ID, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Dispositivo não encontrado");

        verify(deviceRepository, never()).save(any());
        verifyNoInteractions(propertyRepository, deviceMapper);
    }

    @Test
    void update_quandoDeviceIdPertenceAOutroDispositivo_deveLancarResourceAlreadyExistsException() {
        DeviceIO input = inputDeviceIO();
        Device device = device(DEVICE_ID, property());

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));
        when(deviceRepository.existsByDeviceIdAndIdNot(DEVICE_CODE, DEVICE_ID)).thenReturn(true);

        assertThatThrownBy(() -> deviceService.update(DEVICE_ID, input))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Já existe um dispositivo com esse device_id.");

        verify(deviceRepository, never()).save(any());
        verifyNoInteractions(propertyRepository, deviceMapper);
    }

    @Test
    void update_quandoPropriedadeNaoExiste_deveLancarResourceNotFoundException() {
        DeviceIO input = inputDeviceIO();
        Device device = device(DEVICE_ID, property());

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));
        when(deviceRepository.existsByDeviceIdAndIdNot(DEVICE_CODE, DEVICE_ID)).thenReturn(false);
        when(propertyRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.update(DEVICE_ID, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Propriedade não encontrada");

        verify(deviceRepository, never()).save(any());
        verifyNoInteractions(deviceMapper);
    }

    @Test
    void delete_quandoDispositivoExiste_deveExcluirDispositivo() {
        Device device = device(DEVICE_ID, property());
        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));

        deviceService.delete(DEVICE_ID);

        verify(deviceRepository).delete(device);
    }

    @Test
    void delete_quandoDispositivoNaoExiste_deveLancarResourceNotFoundException() {
        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.delete(DEVICE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Dispositivo não encontrado");

        verify(deviceRepository, never()).delete(any());
    }

    private DeviceIO inputDeviceIO() {
        return new DeviceIO(null, DEVICE_CODE, PROPERTY_ID, true, null);
    }

    private DeviceIO savedDeviceIO() {
        return new DeviceIO(DEVICE_ID, DEVICE_CODE, PROPERTY_ID, true, INSTALLATION_DATE);
    }

    private Property property() {
        Property property = new Property();
        property.setId(PROPERTY_ID);
        return property;
    }

    private Device device(Integer id, Property property) {
        return new Device(id, DEVICE_CODE, property, true, INSTALLATION_DATE);
    }
}
