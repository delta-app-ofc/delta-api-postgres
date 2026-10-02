package br.com.delta.delta_api_postgres.modules.auth.controller;

import br.com.delta.delta_api_postgres.modules.auth.security.CurrentUser;
import br.com.delta.delta_api_postgres.modules.auth.service.AuthService;
import br.com.delta.delta_api_postgres.modules.auth.service.TokenService;
import br.com.delta.delta_api_postgres.modules.auth.swagger.AuthSwagger;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/delta/auth")
@RequiredArgsConstructor
public class AuthController implements AuthSwagger {
    private final AuthService auth;
    private final CurrentUser currentUser;

    public record LoginRequest(@NotBlank @Email @Size(max = 255) String email,
                               @NotBlank @Size(max = 256) String password) {}
    public record RegisterRequest(@NotBlank @Size(max = 100) String name,
                                  @NotBlank @Email @Size(max = 255) String email,
                                  @NotBlank @Size(min = 8, max = 256) String password,
                                  @Size(max = 15) String phone,
                                  @NotNull @Past LocalDate birthDate) {}
    public record RefreshTokenRequest(@NotBlank String refreshToken) {}
    public record TokenResponse(String accessToken, String tokenType, long expiresIn,
                                String refreshToken, long refreshExpiresIn) {}
    public record AccessTokenResponse(String accessToken, String tokenType, long expiresIn) {}

    @Override
    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest body) {
        var tokens = auth.register(body.name(), body.email(), body.password(), body.phone(), body.birthDate());
        return tokenResponse(tokens, HttpStatus.CREATED);
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest body) {
        return tokenResponse(auth.login(body.email(), body.password()), HttpStatus.OK);
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest body) {
        AuthService.AccessToken token = auth.refresh(body.refreshToken());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new AccessTokenResponse(token.value(), "Bearer", TokenService.ACCESS_TTL.toSeconds()));
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<AuthService.UserInfo> me() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(auth.me(currentUser.id()));
    }

    private ResponseEntity<TokenResponse> tokenResponse(AuthService.TokenPair tokens, HttpStatus status) {
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore()).body(
                new TokenResponse(
                        tokens.accessToken().value(),
                        "Bearer",
                        TokenService.ACCESS_TTL.toSeconds(),
                        tokens.refreshToken().value(),
                        tokens.refreshToken().expiresIn()
                ));
    }
}
