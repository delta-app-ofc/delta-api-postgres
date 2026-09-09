package br.com.delta.delta_api_postgres.modules.auth.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceAlreadyExistsException;
import br.com.delta.delta_api_postgres.modules.auth.entity.AuthUser;
import br.com.delta.delta_api_postgres.modules.auth.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final AuthUserRepository users;
    private final TokenService tokens;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public record AccessToken(String value) {}
    public record UserInfo(Integer id, String name, String email) {}

    @Transactional
    public AccessToken register(String name, String email, String password, String phone, LocalDate birthDate) {
        String normalized = email.strip().toLowerCase(Locale.ROOT);
        if (users.findByEmail(normalized).isPresent())
            throw new ResourceAlreadyExistsException("Já existe uma conta com este email.");

        AuthUser user = new AuthUser();
        user.setName(name.strip());
        user.setEmail(normalized);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPhone(phone == null || phone.isBlank() ? null : phone.strip());
        user.setBirthDate(birthDate);
        user.setEnabled(true);
        users.saveAndFlush(user);
        return new AccessToken(tokens.accessToken(user, clock.instant()));
    }

    @Transactional(readOnly = true)
    public AccessToken login(String email, String password) {
        String normalized = email.strip().toLowerCase(Locale.ROOT);
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(normalized, password));
        AuthUser user = users.findByEmail(normalized).filter(AuthUser::isEnabled).orElseThrow(AuthService::invalid);
        return new AccessToken(tokens.accessToken(user, clock.instant()));
    }

    @Transactional(readOnly = true)
    public UserInfo me(Integer userId) {
        AuthUser user = users.findById(userId).filter(AuthUser::isEnabled).orElseThrow(AuthService::invalid);
        return new UserInfo(user.getId(), user.getName(), user.getEmail());
    }

    private static BadCredentialsException invalid() {
        return new BadCredentialsException("Credenciais inválidas ou usuário inativo.");
    }
}
