package br.com.delta.delta_api_postgres.modules.user_property.swagger;

import br.com.delta.delta_api_postgres.modules.user_property.dto.io.UserPropertyIO;
import br.com.delta.delta_api_postgres.modules.user_property.dto.request.CreateUserPropertyRequest;
import br.com.delta.delta_api_postgres.modules.user_property.dto.request.UpdateUserPropertyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(
        name = "User Properties",
        description = "O userId deve corresponder ao JWT. DELETE remove somente o próprio vínculo; a associação inicial é feita no cadastro do imóvel."
)
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Sessão inválida ou conta inativa"),
        @ApiResponse(responseCode = "403", description = "Identidade divergente ou operação sem permissão"),
        @ApiResponse(responseCode = "404", description = "Recurso inexistente ou fora do escopo do usuário"),
        @ApiResponse(responseCode = "409", description = "Duplicidade, compartilhamento ou dependência de exclusão")
})
public interface UserPropertySwagger {

    @Operation(summary = "Associar imóvel a um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Associação criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Imóvel não encontrado"),
            @ApiResponse(responseCode = "409", description = "Imóvel já associado a esse usuário")
    })
    ResponseEntity<UserPropertyIO> create(
            Integer userId,
            CreateUserPropertyRequest request
    );

    @Operation(summary = "Listar imóveis associados a um usuário")
    @ApiResponse(responseCode = "200", description = "Associações encontradas")
    ResponseEntity<List<UserPropertyIO>> findAll(Integer userId);

    @Operation(summary = "Consultar associação por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Associação encontrada"),
            @ApiResponse(responseCode = "404", description = "Associação não encontrada")
    })
    ResponseEntity<UserPropertyIO> findById(
            Integer userId,
            Integer id
    );

    @Operation(summary = "Atualizar associação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Associação atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Associação ou imóvel não encontrado"),
            @ApiResponse(responseCode = "409", description = "Imóvel já associado a esse usuário")
    })
    ResponseEntity<UserPropertyIO> update(
            Integer userId,
            Integer id,
            UpdateUserPropertyRequest request
    );

    @Operation(summary = "Excluir associação")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Associação excluída"),
            @ApiResponse(responseCode = "404", description = "Associação não encontrada")
    })
    ResponseEntity<Void> delete(
            Integer userId,
            Integer id
    );
}
