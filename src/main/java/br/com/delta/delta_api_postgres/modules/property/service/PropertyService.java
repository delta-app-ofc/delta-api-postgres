package br.com.delta.delta_api_postgres.modules.property.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.address.entity.Address;
import br.com.delta.delta_api_postgres.modules.address.repository.AddressRepository;
import br.com.delta.delta_api_postgres.modules.organization.entity.Organization;
import br.com.delta.delta_api_postgres.modules.organization.repository.OrganizationRepository;
import br.com.delta.delta_api_postgres.modules.property.dto.io.PropertyIO;
import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import br.com.delta.delta_api_postgres.modules.property.entity.PropertyClassificationEntity;
import br.com.delta.delta_api_postgres.modules.property.mapper.PropertyMapper;
import br.com.delta.delta_api_postgres.modules.property.repository.PropertyClassificationRepository;
import br.com.delta.delta_api_postgres.modules.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {
    private final PropertyRepository propertyRepository;
    private final PropertyClassificationRepository propertyClassificationRepository;
    private final AddressRepository addressRepository;
    private final OrganizationRepository organizationRepository;
    private final PropertyMapper propertyMapper;

    @Transactional
    public PropertyIO create(Integer userId, PropertyIO request) {
        Integer propertyId = propertyRepository.registerProperty(
                userId,
                request.name(),
                request.type().name(),
                request.classification().name(),
                request.addressId(),
                request.organizationId(),
                request.builtAreaM2()
        );

        Property property = propertyRepository.findById(propertyId).orElseThrow(
                () -> new IllegalStateException("A procedure não retornou uma propriedade válida")
        );
        return propertyMapper.toIO(property);
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
        PropertyClassificationEntity classification = propertyClassificationRepository
                .findByName(request.classification())
                .orElseThrow(() -> new ResourceNotFoundException("Classificação não encontrada"));

        Organization organization = null;
        if (request.organizationId() != null) {
            organization = organizationRepository.findById(request.organizationId()).orElseThrow(
                    () -> new ResourceNotFoundException("Organização não encontrada")
            );
        }

        propertyMapper.updateEntity(property, request, address, classification, organization);

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
