package br.com.delta.delta_api_postgres.modules.property.dto.request;

import br.com.delta.delta_api_postgres.modules.property.enums.PropertyClassification;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdatePropertyRequest(
        @NotBlank @Size(max = 100)
        String name,
        @NotNull
        PropertyType type,
        @NotNull
        PropertyClassification classification,
        @NotNull
        @Positive
        Integer addressId,
        @Positive
        Integer organizationId,
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal builtAreaM2
) {
}
