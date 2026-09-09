package br.com.delta.delta_api_postgres.modules.auth.config;

import br.com.delta.delta_api_postgres.modules.auth.repository.AuthUserRepository;
import br.com.delta.delta_api_postgres.modules.auth.security.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.util.List;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class SecurityConfig {
    @Bean Clock clock() { return Clock.systemUTC(); }

    @Bean PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16, 32, 1, 19456, 2);
    }

    @Bean UserDetailsService userDetailsService(AuthUserRepository users) {
        return email -> users.findByEmail(email)
                .map(u -> User.withUsername(u.getEmail()).password(u.getPasswordHash())
                        .authorities(List.of()).disabled(!u.isEnabled()).build())
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas."));
    }

    @Bean AuthenticationManager authenticationManager(UserDetailsService users, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean RSAKey signingKey(AuthProperties properties, ResourceLoader loader) throws Exception {
        if (properties.getPrivateKey().isBlank() || properties.getPublicKey().isBlank())
            throw new IllegalStateException("Configure AUTH_PRIVATE_KEY e AUTH_PUBLIC_KEY com os caminhos file: das chaves RSA PEM.");
        RSAPrivateKey privateKey;
        RSAPublicKey publicKey;
        try (var input = loader.getResource(properties.getPrivateKey()).getInputStream()) {
            privateKey = RsaKeyConverters.pkcs8().convert(input);
        }
        try (var input = loader.getResource(properties.getPublicKey()).getInputStream()) {
            publicKey = RsaKeyConverters.x509().convert(input);
        }
        if (privateKey == null || publicKey == null || publicKey.getModulus().bitLength() < 2048
                || !privateKey.getModulus().equals(publicKey.getModulus()))
            throw new IllegalStateException("As chaves devem formar um par RSA de pelo menos 2048 bits.");
        return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(properties.getKeyId()).build();
    }

    @Bean JwtEncoder jwtEncoder(RSAKey key) {
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
    }

    @Bean JwtDecoder jwtDecoder(RSAKey key, AuthProperties properties, AuthUserRepository users) throws Exception {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(key.toRSAPublicKey())
                .signatureAlgorithm(SignatureAlgorithm.RS256).build();
        OAuth2TokenValidator<Jwt> audience = new JwtClaimValidator<List<String>>("aud",
                aud -> aud != null && aud.contains(properties.getAudience()));
        OAuth2TokenValidator<Jwt> required = jwt -> {
            try {
                if (jwt.getExpiresAt() == null || jwt.getIssuedAt() == null
                        || Integer.parseInt(jwt.getSubject()) <= 0) throw new IllegalArgumentException();
                if (!users.existsByIdAndEnabledTrue(Integer.valueOf(jwt.getSubject())))
                    throw new IllegalArgumentException();
                return OAuth2TokenValidatorResult.success();
            } catch (RuntimeException exception) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
            }
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(java.time.Duration.ZERO),
                new JwtIssuerValidator(properties.getIssuer()), audience, required));
        return decoder;
    }

    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityErrorHandler errors,
                                                 AuthProperties properties, Clock clock) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource(properties)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(cache -> cache.disable())
                .formLogin(form -> form.disable()).httpBasic(basic -> basic.disable()).logout(logout -> logout.disable())
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(errors).accessDeniedHandler(errors))
                .oauth2ResourceServer(resource -> resource.jwt(jwt -> {})
                        .authenticationEntryPoint(errors).accessDeniedHandler(errors))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new AuthRateLimitFilter(properties, errors, clock), CsrfFilter.class);
        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource(AuthProperties properties) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(properties.getAllowedOrigins());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}
