package br.com.delta.delta_api_postgres.modules.device.dto.io;

import br.com.delta.delta_api_postgres.modules.property.enums.PropertyClassificationGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record DeviceOwnerIO(
        @Schema(description = "Identidade canônica do dispositivo, igual à usada na telemetria.", example = "DEVICE-001")
        String deviceId,
        @Schema(description = "Imóvel onde o dispositivo está instalado.", example = "10")
        Integer propertyId,
        @Schema(description = "Grupo da classificação do imóvel.", example = "RESIDENCIAL")
        PropertyClassificationGroup classificationGroup,
        @Schema(description = "Usuários vinculados ao imóvel; pode haver mais de um.")
        List<Integer> userIds,
        @Schema(description = "Se o dispositivo está ativo.")
        Boolean isActive
){}
