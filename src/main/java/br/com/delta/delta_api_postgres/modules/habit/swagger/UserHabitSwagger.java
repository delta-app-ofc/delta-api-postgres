package br.com.delta.delta_api_postgres.modules.habit.swagger;

import br.com.delta.delta_api_postgres.modules.habit.dto.io.UserHabitIO;
import br.com.delta.delta_api_postgres.modules.habit.dto.requests.CreateUserHabitRequest;
import br.com.delta.delta_api_postgres.modules.habit.dto.requests.UpdateUserHabitRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(
        name = "User Habits",
        description = "O userId deve corresponder ao JWT. Apenas os hábitos do próprio usuário podem ser consultados ou alterados."
)
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Sessão inválida ou conta inativa"),
        @ApiResponse(responseCode = "403", description = "Identidade divergente ou operação sem permissão"),
        @ApiResponse(responseCode = "404", description = "Recurso inexistente ou fora do escopo do usuário"),
        @ApiResponse(responseCode = "409", description = "Duplicidade, compartilhamento ou dependência de exclusão")
})
public interface UserHabitSwagger {

    @Operation(summary = "Cadastrar hábito do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Hábito do usuário cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Hábito ou dia da semana não encontrado"),
            @ApiResponse(responseCode = "409", description = "Hábito já cadastrado para o usuário")
    })
    ResponseEntity<UserHabitIO> create(Integer userId, CreateUserHabitRequest request);

    @Operation(summary = "Listar hábitos do usuário")
    @ApiResponse(responseCode = "200", description = "Hábitos do usuário encontrados")
    ResponseEntity<List<UserHabitIO>> findAll(Integer userId);

    @Operation(summary = "Consultar hábito do usuário por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hábito do usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Hábito do usuário não encontrado")
    })
    ResponseEntity<UserHabitIO> findById(Integer userId, Integer id);

    @Operation(summary = "Atualizar hábito do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hábito do usuário atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Hábito do usuário, hábito ou dia da semana não encontrado"),
            @ApiResponse(responseCode = "409", description = "Hábito já cadastrado para o usuário")
    })
    ResponseEntity<UserHabitIO> update(
            Integer userId,
            Integer id,
            UpdateUserHabitRequest request
    );

    @Operation(summary = "Excluir hábito do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Hábito do usuário excluído"),
            @ApiResponse(responseCode = "404", description = "Hábito do usuário não encontrado")
    })
    ResponseEntity<Void> delete(Integer userId, Integer id);
}
