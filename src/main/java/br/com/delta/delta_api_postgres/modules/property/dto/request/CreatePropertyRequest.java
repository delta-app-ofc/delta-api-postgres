package br.com.delta.delta_api_postgres.modules.property.dto.request;

import br.com.delta.delta_api_postgres.modules.property.enums.PropertyClassification;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyType;

public record CreatePropertyRequest(
    String name,
    PropertyType type,
    PropertyClassification classification
) {
}
