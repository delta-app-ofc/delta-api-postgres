package br.com.delta.delta_api_postgres.modules.organization.swagger;

import br.com.delta.delta_api_postgres.modules.organization.dto.io.OrganizationIO;
import br.com.delta.delta_api_postgres.modules.organization.dto.request.CreateOrganizationRequest;
import br.com.delta.delta_api_postgres.modules.organization.dto.request.UpdateOrganizationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(
        name = "Organizations",
        description = "Operações para gerenciamento de organizações (perfil comercial/industrial)"
)
public interface OrganizationSwagger {

    @Operation(summary = "Cadastrar organização")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Organização cadastrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Já existe uma organização com esse CNPJ")
    })
    ResponseEntity<OrganizationIO> create(CreateOrganizationRequest request);

    @Operation(summary = "Listar organizações")
    @ApiResponse(responseCode = "200", description = "Organizações encontradas")
    ResponseEntity<List<OrganizationIO>> findAll();

    @Operation(summary = "Consultar organização por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organização encontrada"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    ResponseEntity<OrganizationIO> findById(Integer id);

    @Operation(summary = "Atualizar organização")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organização atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada"),
            @ApiResponse(responseCode = "409", description = "Já existe uma organização com esse CNPJ")
    })
    ResponseEntity<OrganizationIO> update(
            Integer id,
            UpdateOrganizationRequest request
    );

    @Operation(summary = "Excluir organização")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Organização excluída"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada"),
            @ApiResponse(responseCode = "409", description = "Organização vinculada a outro recurso")
    })
    ResponseEntity<Void> delete(Integer id);
}
