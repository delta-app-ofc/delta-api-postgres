package br.com.delta.delta_api_postgres.modules.auth.service;

import br.com.delta.delta_api_postgres.modules.auth.config.AuthProperties;
import br.com.delta.delta_api_postgres.modules.auth.entity.AuthUser;
import br.com.delta.delta_api_postgres.modules.auth.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh:";
    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;
    private final AuthUserRepository users;
    private final TokenService tokens;
    private final AuthProperties properties;
    private final Clock clock;

    public CreatedRefreshToken create(AuthUser user) {
        byte[] randomBytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        redisTemplate.opsForValue().set(
                redisKey(refreshToken),
                user.getId().toString(),
                properties.getRefreshTtl()
        );

        return new CreatedRefreshToken(refreshToken, properties.getRefreshTtl().toSeconds());
    }

    @Transactional(readOnly = true)
    public String refreshAccessToken(String refreshToken) {
        String storedUserId = redisTemplate.opsForValue().get(redisKey(refreshToken));
        if (storedUserId == null) {
            throw invalid();
        }

        AuthUser user;
        try {
            user = users.findById(Integer.valueOf(storedUserId))
                    .filter(AuthUser::isEnabled)
                    .orElseThrow(RefreshTokenService::invalid);
        } catch (NumberFormatException exception) {
            throw invalid();
        }

        return tokens.accessToken(user, clock.instant());
    }

    private String redisKey(String refreshToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível.", exception);
        }
    }

    private static BadCredentialsException invalid() {
        return new BadCredentialsException("Refresh token inválido ou expirado.");
    }

    public record CreatedRefreshToken(String value, long expiresIn) {
    }
}
