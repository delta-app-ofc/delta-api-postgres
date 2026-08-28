package br.com.delta.delta_api_postgres.modules.property.mapper;

import br.com.delta.delta_api_postgres.modules.property.dto.io.PropertyIO;
import br.com.delta.delta_api_postgres.modules.property.dto.request.CreatePropertyRequest;
import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import org.springframework.stereotype.Component;

@Component
public class PropertyMapper {
    public PropertyIO fromCreateToIO(CreatePropertyRequest request) {
        return new PropertyIO(
                null,
                request.name(),
                request.type(),
                request.classification(),
                request.addressId(),
                null
        );
    }

    public PropertyIO toIO(Property property) {
        return new PropertyIO(
                property.getId(),
                property.getName(),
                property.getType(),
                property.getClassification(),
                property.getAddressId(),
                property.getRegistrationDate()
        );
    }

    public Property toEntity(PropertyIO io) {
        return new Property(
                io.id(),
                io.name(),
                io.type(),
                io.classification(),
                io.adressId(),
                io.registrationDate()
        );
    }
}
