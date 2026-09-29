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
        description = "Operações para gerenciamento de dispositivos"
)
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

    @Operation(summary = "Atualizar dispositivo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dispositivo atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Dispositivo ou propriedade não encontrado"),
            @ApiResponse(responseCode = "409", description = "Dispositivo já cadastrado")
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
