package br.com.delta.delta_api_postgres.modules.device.security;

import br.com.delta.delta_api_postgres.modules.auth.security.SecurityErrorHandler;
import br.com.delta.delta_api_postgres.modules.device.config.DeviceIntegrationProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class MongoServiceAuthenticationFilter extends OncePerRequestFilter {
    public static final String VALIDATE_AUTHORITY = "DEVICE_AUTH_VALIDATE";
    private final DeviceIntegrationProperties properties;
    private final SecurityErrorHandler errors;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        List<String> headers = Collections.list(request.getHeaders(HttpHeaders.AUTHORIZATION));
        String expected = properties.getMongoCredential();
        String header = headers.size() == 1 ? headers.get(0) : "";
        String supplied = header.regionMatches(true, 0, "Bearer ", 0, 7) ? header.substring(7) : "";
        if (expected.isEmpty() || supplied.length() > 256 || !supplied.matches("delta_svc_[A-Za-z0-9_-]{43,128}")
                || !MessageDigest.isEqual(digest(expected), digest(supplied))) {
            errors.commence(request, response, new BadCredentialsException("Credencial de integração inválida."));
            return;
        }
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("mongo-api", null,
                List.of(new SimpleGrantedAuthority(VALIDATE_AUTHORITY))));
        SecurityContextHolder.setContext(context);
        chain.doFilter(request, response);
    }

    private byte[] digest(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível.", exception);
        }
    }
}
