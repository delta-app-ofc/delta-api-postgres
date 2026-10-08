package br.com.delta.delta_api_postgres.modules.user_property.dto.io;

import java.time.LocalDate;

public record UserPropertyIO(
        Integer id,
        Integer userId,
        Integer propertyId,
        String propertyName,
        LocalDate associationDate
) {
}
