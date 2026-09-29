package br.com.delta.delta_api_postgres.modules.property.dto.params;

import br.com.delta.delta_api_postgres.modules.property.enums.PropertyClassification;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyType;

public record PropertyParams(
        PropertyType propertyType,
        PropertyClassification propertyClassification

) {
}
