package br.com.delta.delta_api_postgres.modules.user_property.mapper;

import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import br.com.delta.delta_api_postgres.modules.user_property.dto.io.UserPropertyIO;
import br.com.delta.delta_api_postgres.modules.user_property.dto.request.CreateUserPropertyRequest;
import br.com.delta.delta_api_postgres.modules.user_property.dto.request.UpdateUserPropertyRequest;
import br.com.delta.delta_api_postgres.modules.user_property.entity.UserProperty;
import org.springframework.stereotype.Component;

@Component
public class UserPropertyMapper {

    public UserPropertyIO fromCreateRequest(
            Integer userId,
            CreateUserPropertyRequest request
    ) {
        return new UserPropertyIO(
                null,
                userId,
                request.propertyId(),
                null,
                null
        );
    }

    public UserPropertyIO fromUpdateRequest(
            Integer id,
            Integer userId,
            UpdateUserPropertyRequest request
    ) {
        return new UserPropertyIO(
                id,
                userId,
                request.propertyId(),
                null,
                null
        );
    }

    public UserPropertyIO toIO(UserProperty userProperty) {

        return new UserPropertyIO(
                userProperty.getId(),
                userProperty.getUserId(),
                userProperty.getProperty().getId(),
                userProperty.getProperty().getName(),
                userProperty.getAssociationDate()
        );
    }

    public UserProperty toEntity(
            UserPropertyIO io,
            Property property
    ) {

        UserProperty userProperty = new UserProperty();

        userProperty.setUserId(io.userId());
        userProperty.setProperty(property);

        return userProperty;
    }
}
