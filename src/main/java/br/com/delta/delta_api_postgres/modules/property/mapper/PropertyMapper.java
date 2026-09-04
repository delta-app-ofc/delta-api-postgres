package br.com.delta.delta_api_postgres.modules.property.mapper;

import br.com.delta.delta_api_postgres.modules.address.entity.Address;
import br.com.delta.delta_api_postgres.modules.property.dto.io.PropertyIO;
import br.com.delta.delta_api_postgres.modules.property.dto.request.CreatePropertyRequest;
import br.com.delta.delta_api_postgres.modules.property.dto.request.UpdatePropertyRequest;
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
    public PropertyIO fromUpdateRequest(
            Integer id,
            UpdatePropertyRequest request
    ) {
        return new PropertyIO(
                id,
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
                property.getAddress().getId(),
                property.getRegistrationDate()
        );
    }

    public Property toEntity(PropertyIO io, Address address) {
        return new Property(
                io.id(),
                io.name(),
                io.type(),
                io.classification(),
                address,
                io.registrationDate()
        );
    }

    public void updateEntity(Property property, PropertyIO io, Address address) {
        property.setName(io.name());
        property.setType(io.type());
        property.setClassification(io.classification());
        property.setAddress(address);
    }
}
