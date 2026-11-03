package br.com.delta.delta_api_postgres.modules.auth.dto;

import br.com.delta.delta_api_postgres.modules.address.dto.request.CreateAddressRequest;
import br.com.delta.delta_api_postgres.modules.habit.dto.requests.CreateUserHabitRequest;
import br.com.delta.delta_api_postgres.modules.organization.dto.request.CreateOrganizationRequest;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyClassification;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record CompleteRegistrationRequest(
        @NotNull @Valid CreateAddressRequest address,
        @NotNull @Valid PropertyDetails property,
        @NotNull @Size(max = 100) List<@NotNull @Valid CreateUserHabitRequest> habits,
        /** Opcional: cria a organização junto com a primeira instalação (o usuário passa a pertencer a ela pela instalação). */
        @Valid CreateOrganizationRequest organization
) {
    public CompleteRegistrationRequest(CreateAddressRequest address, PropertyDetails property,
                                       List<CreateUserHabitRequest> habits) {
        this(address, property, habits, null);
    }

    public record PropertyDetails(
            @NotBlank @Size(max = 100) String name,
            @NotNull PropertyType type,
            @NotNull PropertyClassification classification,
            @Positive Integer organizationId,
            @DecimalMin(value = "0.0", inclusive = false) BigDecimal builtAreaM2
    ) {}
}
