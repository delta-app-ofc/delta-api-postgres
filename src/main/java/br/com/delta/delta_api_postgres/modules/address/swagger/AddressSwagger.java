package br.com.delta.delta_api_postgres.modules.address.swagger;

import br.com.delta.delta_api_postgres.modules.address.dto.io.AddressIO;
import br.com.delta.delta_api_postgres.modules.address.dto.request.CreateAddressRequest;
import br.com.delta.delta_api_postgres.modules.address.dto.request.UpdateAddressRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(
        name = "Addresses",
        description = "Endereços consultados pelo vínculo usuário–imóvel. Alteração de endereço compartilhado ou exclusão de endereço referenciado retorna 409."
)
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Sessão inválida ou conta inativa"),
        @ApiResponse(responseCode = "403", description = "Identidade divergente ou operação sem permissão"),
        @ApiResponse(responseCode = "404", description = "Recurso inexistente ou fora do escopo do usuário"),
        @ApiResponse(responseCode = "409", description = "Duplicidade, compartilhamento ou dependência de exclusão")
})
public interface AddressSwagger {

    @Operation(summary = "Cadastrar endereço")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Endereço cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Região não encontrada")
    })
    ResponseEntity<AddressIO> create(CreateAddressRequest request);

    @Operation(summary = "Listar endereços")
    @ApiResponse(responseCode = "200", description = "Endereços encontrados")
    ResponseEntity<List<AddressIO>> findAll();

    @Operation(summary = "Consultar endereço por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço encontrado"),
            @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    ResponseEntity<AddressIO> findById(Integer id);

    @Operation(summary = "Atualizar endereço")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Endereço ou região não encontrado")
    })
    ResponseEntity<AddressIO> update(
            Integer id,
            UpdateAddressRequest request
    );

    @Operation(summary = "Excluir endereço")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Endereço excluído"),
            @ApiResponse(responseCode = "404", description = "Endereço não encontrado"),
            @ApiResponse(responseCode = "409", description = "Endereço vinculado a outro recurso")
    })
    ResponseEntity<Void> delete(Integer id);
}