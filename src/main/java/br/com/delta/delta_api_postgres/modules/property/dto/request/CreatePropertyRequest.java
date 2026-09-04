package br.com.delta.delta_api_postgres.modules.property.dto.request;

import br.com.delta.delta_api_postgres.modules.address.entity.Address;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyClassification;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreatePropertyRequest(
        @NotBlank @Size(max = 100)
        String name,
        @NotNull
        PropertyType type,
        @NotNull
        PropertyClassification classification,
        Address address
) {
}
