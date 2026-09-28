package br.com.delta.delta_api_postgres.modules.property.dto.io;

import br.com.delta.delta_api_postgres.modules.property.enums.PropertyClassification;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyType;

import java.time.LocalDate;

public record PropertyIO(
        Integer id,
        String name,
        PropertyType type,
        PropertyClassification classification,
        Integer addressId,
        LocalDate registrationDate
) {
}
