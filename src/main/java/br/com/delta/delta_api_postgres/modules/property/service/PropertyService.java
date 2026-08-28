package br.com.delta.delta_api_postgres.modules.property.service;

import br.com.delta.delta_api_postgres.modules.property.dto.io.PropertyIO;
import br.com.delta.delta_api_postgres.modules.property.dto.request.CreatePropertyRequest;
import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import br.com.delta.delta_api_postgres.modules.property.mapper.PropertyMapper;
import br.com.delta.delta_api_postgres.modules.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PropertyService {
    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    public PropertyIO create(CreatePropertyRequest request) {
        PropertyIO io = propertyMapper.fromCreateToIO(request);

        Property property = propertyRepository.save(propertyMapper.toEntity(io));

        return propertyMapper.toIO(property);
    }
}
