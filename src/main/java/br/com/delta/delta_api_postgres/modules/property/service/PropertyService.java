package br.com.delta.delta_api_postgres.modules.property.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.address.entity.Address;
import br.com.delta.delta_api_postgres.modules.address.repository.AddressRepository;
import br.com.delta.delta_api_postgres.modules.property.dto.io.PropertyIO;
import br.com.delta.delta_api_postgres.modules.property.dto.request.CreatePropertyRequest;
import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import br.com.delta.delta_api_postgres.modules.property.mapper.PropertyMapper;
import br.com.delta.delta_api_postgres.modules.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {
    private final PropertyRepository propertyRepository;
    private final AddressRepository addressRepository;
    private final PropertyMapper propertyMapper;

    public PropertyIO create(PropertyIO request) {
        Address address = addressRepository.findById(request.addressId()).orElseThrow(
                () -> new ResourceNotFoundException("Endereço nao encontrado")
        );
        Property property = propertyMapper.toEntity(request, address);

        Property savedProperty = propertyRepository.save(property);
        return propertyMapper.toIO(savedProperty);
    }
    @Transactional(readOnly = true)
    public List<PropertyIO> findAll() {
        return propertyRepository.findAll().stream().map(propertyMapper::toIO).toList();
    }
    @Transactional(readOnly = true)
    public PropertyIO findById(Integer id) {
        Property property = propertyRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Propriedade nao encontrada")
        );
        return propertyMapper.toIO(property);
    }
    public PropertyIO update(Integer id, PropertyIO request) {
        Property property = propertyRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Propriedade nao encontrada")
        );
        Address address = addressRepository.findById(request.addressId()).orElseThrow(
                () -> new ResourceNotFoundException("Endereço nao encontrado")
        );
        propertyMapper.updateEntity(property, request, address);

        Property updatedProperty = propertyRepository.save(property);

        return propertyMapper.toIO(updatedProperty);
    }
    public void delete(Integer id) {
        Property property = propertyRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Propriedade nao encontrada")
        );
        propertyRepository.delete(property);
    }
}
