package br.com.delta.delta_api_postgres.modules.user_property.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateUserPropertyRequest(

        @NotNull(message = "Property ID é obrigatório")
        @Positive(message = "Property ID deve ser positivo")
        Integer propertyId

) {
}
