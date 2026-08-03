package br.com.delta.delta_api_postgres.dto.io;

import java.time.LocalDate;

public record DeviceIO(
        Integer id,
        Integer deviceId,
        Integer propertyId,
        Boolean isActive,
        LocalDate installationDate
){}