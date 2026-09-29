package br.com.delta.delta_api_postgres.modules.property.mapper;

import br.com.delta.delta_api_postgres.modules.address.entity.Address;
import br.com.delta.delta_api_postgres.modules.organization.entity.Organization;
import br.com.delta.delta_api_postgres.modules.property.dto.io.PropertyIO;
import br.com.delta.delta_api_postgres.modules.property.dto.request.CreatePropertyRequest;
import br.com.delta.delta_api_postgres.modules.property.dto.request.UpdatePropertyRequest;
import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import br.com.delta.delta_api_postgres.modules.property.entity.PropertyClassificationEntity;
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
                request.organizationId(),
                request.builtAreaM2(),
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
                request.organizationId(),
                request.builtAreaM2(),
                null
        );
    }

    public PropertyIO toIO(Property property) {
        return new PropertyIO(
                property.getId(),
                property.getName(),
                property.getType(),
                property.getClassification().getName(),
                property.getAddress().getId(),
                property.getOrganization() != null ? property.getOrganization().getId() : null,
                property.getBuiltAreaM2(),
                property.getRegistrationDate()
        );
    }

    public void updateEntity(
            Property property,
            PropertyIO io,
            Address address,
            PropertyClassificationEntity classification,
            Organization organization
    ) {
        property.setName(io.name());
        property.setType(io.type());
        property.setClassification(classification);
        property.setAddress(address);
        property.setOrganization(organization);
        property.setBuiltAreaM2(io.builtAreaM2());
    }
}
