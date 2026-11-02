package br.com.delta.delta_api_postgres.modules.auth.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceAlreadyExistsException;
import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.property.enums.PropertyClassificationGroup;
import br.com.delta.delta_api_postgres.modules.auth.entity.AuthUser;
import br.com.delta.delta_api_postgres.modules.auth.dto.UpdateProfileRequest;
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
    private final RefreshTokenService refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public record AccessToken(String value) {}
    public record TokenPair(AccessToken accessToken, RefreshTokenService.CreatedRefreshToken refreshToken) {}
    public record UserInfo(Integer id, String name, String email, String phone, LocalDate birthDate) {}
    public record ClassificationResponse(PropertyClassificationGroup classificationGroup) {}

    @Transactional(readOnly = true)
    public ClassificationResponse classificationByEmail(String email) {
        var groups = users.findClassificationGroupsByEmail(email.strip().toLowerCase(Locale.ROOT));
        if (groups.isEmpty())
            throw new ResourceNotFoundException("Classificação não encontrada para este e-mail.");
        if (groups.size() > 1)
            throw new ResourceAlreadyExistsException("Usuário possui imóveis residenciais e comerciais.");
        return new ClassificationResponse(groups.get(0));
    }

    @Transactional
    public TokenPair register(String name, String email, String password, String phone, LocalDate birthDate) {
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
        return createTokenPair(user);
    }

    @Transactional(readOnly = true)
    public TokenPair login(String email, String password) {
        String normalized = email.strip().toLowerCase(Locale.ROOT);
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(normalized, password));
        AuthUser user = users.findByEmail(normalized).filter(AuthUser::isEnabled).orElseThrow(AuthService::invalid);
        return createTokenPair(user);
    }

    @Transactional(readOnly = true)
    public AccessToken refresh(String refreshToken) {
        return new AccessToken(refreshTokens.refreshAccessToken(refreshToken));
    }

    @Transactional(readOnly = true)
    public UserInfo me(Integer userId) {
        AuthUser user = users.findById(userId).filter(AuthUser::isEnabled).orElseThrow(AuthService::invalid);
        return userInfo(user);
    }

    @Transactional
    public UserInfo updateProfile(Integer userId, UpdateProfileRequest request) {
        AuthUser user = users.lockById(userId).filter(AuthUser::isEnabled).orElseThrow(AuthService::invalid);
        users.findByEmail(request.email()).filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> { throw new ResourceAlreadyExistsException("Já existe uma conta com este email."); });
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setBirthDate(request.birthDate());
        return userInfo(users.saveAndFlush(user));
    }

    /** Logical deletion preserves shared resources and makes all existing tokens unusable. */
    @Transactional
    public void deleteProfile(Integer userId) {
        AuthUser user = users.lockById(userId).filter(AuthUser::isEnabled).orElseThrow(AuthService::invalid);
        user.setEnabled(false);
        users.saveAndFlush(user);
    }

    private UserInfo userInfo(AuthUser user) {
        return new UserInfo(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getBirthDate());
    }

    private static BadCredentialsException invalid() {
        return new BadCredentialsException("Credenciais inválidas ou usuário inativo.");
    }

    private TokenPair createTokenPair(AuthUser user) {
        AccessToken accessToken = new AccessToken(tokens.accessToken(user, clock.instant()));
        return new TokenPair(accessToken, refreshTokens.create(user));
    }
}
