package br.com.delta.delta_api_postgres.modules.device.swagger;

import br.com.delta.delta_api_postgres.modules.device.dto.io.DeviceIO;
import br.com.delta.delta_api_postgres.modules.device.dto.request.CreateDeviceRequest;
import br.com.delta.delta_api_postgres.modules.device.dto.request.UpdateDeviceRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(
        name = "Devices",
        description = "Dispositivos dos imóveis vinculados ao usuário autenticado. Alterações em imóvel compartilhado retornam 409; deviceId é imutável."
)
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Sessão inválida ou conta inativa"),
        @ApiResponse(responseCode = "403", description = "Identidade divergente ou operação sem permissão"),
        @ApiResponse(responseCode = "404", description = "Recurso inexistente ou fora do escopo do usuário"),
        @ApiResponse(responseCode = "409", description = "Duplicidade, compartilhamento ou dependência de exclusão")
})
public interface DeviceSwagger {

    @Operation(summary = "Cadastrar dispositivo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dispositivo cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada"),
            @ApiResponse(responseCode = "409", description = "Dispositivo já cadastrado")
    })
    ResponseEntity<DeviceIO> create(CreateDeviceRequest request);

    @Operation(summary = "Listar dispositivos")
    @ApiResponse(responseCode = "200", description = "Dispositivos encontrados")
    ResponseEntity<List<DeviceIO>> findAll();

    @Operation(summary = "Consultar dispositivo por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dispositivo encontrado"),
            @ApiResponse(responseCode = "404", description = "Dispositivo não encontrado")
    })
    ResponseEntity<DeviceIO> findById(Integer id);

    @Operation(summary = "Atualizar dispositivo", description = "Atualiza propriedade e status. O deviceId deve permanecer igual ao cadastrado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dispositivo atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Dispositivo ou propriedade não encontrado"),
            @ApiResponse(responseCode = "409", description = "Alteração da identidade deviceId não permitida")
    })
    ResponseEntity<DeviceIO> update(
            Integer id,
            UpdateDeviceRequest request
    );

    @Operation(summary = "Excluir dispositivo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Dispositivo excluído"),
            @ApiResponse(responseCode = "404", description = "Dispositivo não encontrado"),
            @ApiResponse(responseCode = "409", description = "Dispositivo vinculado a outro recurso")
    })
    ResponseEntity<Void> delete(Integer id);
}
