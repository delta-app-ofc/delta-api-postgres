package br.com.delta.delta_api_postgres.modules.auth.services;

import br.com.delta.delta_api_postgres.modules.auth.config.AuthProperties;
import br.com.delta.delta_api_postgres.modules.auth.entity.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TokenService {
    public static final Duration ACCESS_TTL = Duration.ofMinutes(15);
    private final JwtEncoder encoder;
    private final AuthProperties properties;

    public String accessToken(AuthUser user, Instant now) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer()).audience(List.of(properties.getAudience()))
                .subject(user.getId().toString()).id(UUID.randomUUID().toString())
                .issuedAt(now).expiresAt(now.plus(ACCESS_TTL)).build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(properties.getKeyId()).type("JWT").build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

}
