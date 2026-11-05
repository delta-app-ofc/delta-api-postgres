package br.com.delta.delta_api_postgres.modules.auth.service;

import br.com.delta.delta_api_postgres.modules.auth.dto.ResetPasswordRequest;
import br.com.delta.delta_api_postgres.modules.auth.entity.AuthUser;
import br.com.delta.delta_api_postgres.modules.auth.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.Cursor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    public static final String GENERIC_RESPONSE = "Se existir uma conta ativa com esse e-mail, enviaremos as instruções de recuperação.";
    private static final String TOKEN_PREFIX = "password-reset:";
    private static final String REFRESH_PREFIX = "refresh:";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthUserRepository users;
    private final StringRedisTemplate redis;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetEmailSender emailSender;
    private final RefreshTokenService refreshTokens;

    public String request(String email) {
        if (!emailSender.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "A recuperação de senha está temporariamente indisponível.");
        }
        Optional<AuthUser> account = users.findByEmail(email.strip().toLowerCase(java.util.Locale.ROOT))
                .filter(AuthUser::isEnabled);
        if (account.isEmpty()) return GENERIC_RESPONSE;

        AuthUser user = account.get();
        byte[] random = new byte[32];
        RANDOM.nextBytes(random);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        String key = tokenKey(token);
        redis.opsForValue().set(key, user.getId().toString(), TOKEN_TTL);
        try {
            emailSender.send(user.getEmail(), user.getName(), token);
        } catch (RuntimeException exception) {
            redis.delete(key);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "A recuperação de senha está temporariamente indisponível.");
        }
        return GENERIC_RESPONSE;
    }

    @Transactional
    public void reset(ResetPasswordRequest request) {
        String storedUserId = redis.opsForValue().getAndDelete(tokenKey(request.token()));
        Integer userId;
        try {
            userId = storedUserId == null ? null : Integer.valueOf(storedUserId);
        } catch (NumberFormatException exception) {
            userId = null;
        }
        if (userId == null) throw invalidToken();

        AuthUser user = users.lockById(userId).filter(AuthUser::isEnabled).orElseThrow(PasswordResetService::invalidToken);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        users.saveAndFlush(user);
        refreshTokens.revokeAllForUser(userId);
        revokePasswordResetTokensForUser(userId);
    }

    private static String tokenKey(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return TOKEN_PREFIX + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível.", exception);
        }
    }

    private void revokePasswordResetTokensForUser(Integer userId) {
        String expectedUserId = userId.toString();
        try (var cursor = redis.scan(ScanOptions.scanOptions().match(TOKEN_PREFIX + "*").count(500).build())) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                if (expectedUserId.equals(redis.opsForValue().get(key))) redis.delete(key);
            }
        }
    }

    private static ResponseStatusException invalidToken() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "O link de recuperação é inválido ou expirou. Solicite outro.");
    }
}
