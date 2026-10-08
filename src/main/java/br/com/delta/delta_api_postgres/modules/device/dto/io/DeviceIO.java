package br.com.delta.delta_api_postgres.modules.device.dto.io;

import java.time.LocalDate;

public record DeviceIO(
        @io.swagger.v3.oas.annotations.media.Schema(description = "ID inteiro interno do SQL, usado nas rotas /delta/device/{id}.", example = "1")
        Integer id,
        @io.swagger.v3.oas.annotations.media.Schema(description = "Identidade canônica única e imutável do device, compartilhada com a telemetria Mongo. Comparação exata, sem normalização.", example = "DEVICE-001")
        String deviceId,
        Integer propertyId,
        Boolean isActive,
        LocalDate installationDate
){}
