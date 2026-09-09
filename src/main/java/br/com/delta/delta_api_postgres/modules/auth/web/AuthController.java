package br.com.delta.delta_api_postgres.modules.auth.web;

import br.com.delta.delta_api_postgres.modules.auth.security.CurrentUser;
import br.com.delta.delta_api_postgres.modules.auth.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;
    private final CurrentUser currentUser;

    public record LoginRequest(@NotBlank @Email @Size(max = 255) String email,
                               @NotBlank @Size(max = 256) String password) {}
    public record RegisterRequest(@NotBlank @Size(max = 100) String name,
                                  @NotBlank @Email @Size(max = 255) String email,
                                  @NotBlank @Size(min = 8, max = 256) String password,
                                  @Size(max = 15) String phone,
                                  @NotNull @Past LocalDate birthDate) {}
    public record TokenResponse(String accessToken, String tokenType, long expiresIn) {}

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest body) {
        var token = auth.register(body.name(), body.email(), body.password(), body.phone(), body.birthDate());
        return tokenResponse(token, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest body) {
        return tokenResponse(auth.login(body.email(), body.password()), HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthService.UserInfo> me() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(auth.me(currentUser.id()));
    }

    private ResponseEntity<TokenResponse> tokenResponse(AuthService.AccessToken token, HttpStatus status) {
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore()).body(
                new TokenResponse(token.value(), "Bearer", TokenService.ACCESS_TTL.toSeconds()));
    }
}
