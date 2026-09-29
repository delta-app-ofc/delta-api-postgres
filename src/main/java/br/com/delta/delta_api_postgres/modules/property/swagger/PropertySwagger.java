package br.com.delta.delta_api_postgres.modules.property.swagger;

import br.com.delta.delta_api_postgres.modules.property.dto.io.PropertyIO;
import br.com.delta.delta_api_postgres.modules.property.dto.request.CreatePropertyRequest;
import br.com.delta.delta_api_postgres.modules.property.dto.request.UpdatePropertyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(
        name = "Properties",
        description = "Operações para gerenciamento de propriedades"
)
public interface PropertySwagger {

    @Operation(summary = "Cadastrar propriedade")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Propriedade cadastrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    ResponseEntity<PropertyIO> create(CreatePropertyRequest request);

    @Operation(summary = "Listar propriedades")
    @ApiResponse(responseCode = "200", description = "Propriedades encontradas")
    ResponseEntity<List<PropertyIO>> findAll();

    @Operation(summary = "Consultar propriedade por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propriedade encontrada"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada")
    })
    ResponseEntity<PropertyIO> findById(Integer id);

    @Operation(summary = "Atualizar propriedade")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propriedade atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Propriedade ou endereço não encontrado")
    })
    ResponseEntity<PropertyIO> update(
            Integer id,
            UpdatePropertyRequest request
    );

    @Operation(summary = "Excluir propriedade")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Propriedade excluída"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada"),
            @ApiResponse(responseCode = "409", description = "Propriedade vinculada a outro recurso")
    })
    ResponseEntity<Void> delete(Integer id);
}
