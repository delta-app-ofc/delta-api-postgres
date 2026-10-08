package br.com.delta.delta_api_postgres.modules.auth.swagger;

import br.com.delta.delta_api_postgres.modules.auth.dto.CompleteRegistrationRequest;
import br.com.delta.delta_api_postgres.modules.auth.service.RegistrationCompletionService;
import br.com.delta.delta_api_postgres.modules.auth.service.AuthService;
import br.com.delta.delta_api_postgres.modules.auth.controller.AuthController.LoginRequest;
import br.com.delta.delta_api_postgres.modules.auth.controller.AuthController.AccessTokenResponse;
import br.com.delta.delta_api_postgres.modules.auth.controller.AuthController.RefreshTokenRequest;
import br.com.delta.delta_api_postgres.modules.auth.controller.AuthController.RegisterRequest;
import br.com.delta.delta_api_postgres.modules.auth.controller.AuthController.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "Authentication",
        description = "Operações para autenticação e identificação do usuário"
)
public interface AuthSwagger {

    @Operation(summary = "Cadastrar usuário", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado e autenticado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
    })
    ResponseEntity<TokenResponse> createAccount(RegisterRequest body);

    @Operation(summary = "Autenticar usuário", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário autenticado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas ou usuário inativo")
    })
    ResponseEntity<TokenResponse> login(LoginRequest body);

    @Operation(summary = "Renovar access token", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Novo access token gerado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado")
    })
    ResponseEntity<AccessTokenResponse> refresh(RefreshTokenRequest body);

    @Operation(
            summary = "Consultar usuário autenticado",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário autenticado encontrado"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária ou token inválido")
    })
    ResponseEntity<AuthService.UserInfo> me();
    @Operation(summary = "Conclui o cadastro com endereço, imóvel e hábitos",
            description = "Usa o usuário do token. Reenvios com os mesmos dados retornam o cadastro existente.")
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<RegistrationCompletionService.Result> complete(CompleteRegistrationRequest request);
}
