package br.com.delta.delta_api_postgres.service;

import br.com.delta.delta_api_postgres.dto.io.DeviceIO;
import br.com.delta.delta_api_postgres.entity.Device;
import br.com.delta.delta_api_postgres.entity.Property;
import br.com.delta.delta_api_postgres.mapper.DeviceMapper;
import br.com.delta.delta_api_postgres.repository.DeviceRepository;
import br.com.delta.delta_api_postgres.repository.PropertyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final PropertyRepository propertyRepository;
    private final DeviceMapper deviceMapper;


    public DeviceIO create(DeviceIO io) {

        if(deviceRepository.existsByDeviceId(io.deviceId())){
            throw new IllegalArgumentException(
                    "Device já cadastrado"
            );
        }


        Property property = propertyRepository.findById(io.propertyId())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Property não encontrada"
                        )
                );


        Device device = deviceMapper.toEntity(
                io,
                property
        );


        Device saved = deviceRepository.save(device);


        return deviceMapper.toIO(saved);
    }



    @Transactional(readOnly = true)
    public List<DeviceIO> findAll(){

        return deviceRepository.findAll()
                .stream()
                .map(deviceMapper::toIO)
                .toList();
    }



    @Transactional(readOnly = true)
    public DeviceIO findById(Integer id){

        Device device = deviceRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Device não encontrado"
                        )
                );


        return deviceMapper.toIO(device);
    }



    public DeviceIO update(
            Integer id,
            DeviceIO io
    ){

        Device device = deviceRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Device não encontrado"
                        )
                );


        Property property = propertyRepository.findById(io.propertyId())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Property não encontrada"
                        )
                );


        deviceMapper.updateEntity(
                device,
                io,
                property
        );


        Device updated = deviceRepository.save(device);


        return deviceMapper.toIO(updated);
    }



    public void delete(Integer id){

        Device device = deviceRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Device não encontrado"
                        )
                );


        deviceRepository.delete(device);
    }

}
