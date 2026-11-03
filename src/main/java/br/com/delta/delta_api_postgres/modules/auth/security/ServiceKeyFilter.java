package br.com.delta.delta_api_postgres.modules.auth.security;

import br.com.delta.delta_api_postgres.modules.auth.config.AuthProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Autentica chamadas entre serviços (ex.: delta-api-mongo) nas rotas /delta/internal/**.
 * O chamador envia a chave no cabeçalho X-Service-Key; ela vem da variável AUTH_SERVICE_KEY.
 * Se a chave não estiver configurada, as rotas internas ficam fechadas para todos.
 */
public class ServiceKeyFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Service-Key";
    public static final String ROLE = "SERVICE";
    private static final String INTERNAL_PREFIX = "/delta/internal/";

    private final AuthProperties properties;
    private final SecurityErrorHandler errors;

    public ServiceKeyFilter(AuthProperties properties, SecurityErrorHandler errors) {
        this.properties = properties;
        this.errors = errors;
    }

    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().substring(request.getContextPath().length()).startsWith(INTERNAL_PREFIX);
    }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                               FilterChain chain) throws ServletException, IOException {
        if (!validKey(request.getHeader(HEADER))) {
            errors.write(request, response, HttpStatus.UNAUTHORIZED, "Chave de serviço ausente ou inválida.");
            return;
        }
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                "service", null, List.of(new SimpleGrantedAuthority("ROLE_" + ROLE)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    // Comparação em tempo constante para não vazar a chave por diferença de tempo.
    private boolean validKey(String received) {
        String expected = properties.getServiceKey();
        if (expected == null || expected.isBlank() || received == null) return false;
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), received.getBytes(StandardCharsets.UTF_8));
    }
}
