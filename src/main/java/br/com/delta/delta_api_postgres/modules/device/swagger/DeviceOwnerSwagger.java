package br.com.delta.delta_api_postgres.modules.device.swagger;

import br.com.delta.delta_api_postgres.modules.device.dto.io.DeviceOwnerIO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "Devices (interno)",
        description = "Consulta entre serviços. Exige o cabeçalho X-Service-Key; não usa o token de usuário."
)
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Chave de serviço ausente ou inválida"),
        @ApiResponse(responseCode = "404", description = "Dispositivo não encontrado")
})
public interface DeviceOwnerSwagger {

    @Operation(summary = "Consultar imóvel, usuários e classificação de um dispositivo")
    @ApiResponse(responseCode = "200", description = "Dono encontrado")
    ResponseEntity<DeviceOwnerIO> findOwner(String deviceId);
}
