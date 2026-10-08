package br.com.delta.delta_api_postgres.modules.auth.security;

import br.com.delta.delta_api_postgres.modules.auth.config.AuthProperties;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Clock;
import java.util.*;

/** Per-instance guard; use a shared gateway limit when deploying multiple replicas. */
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private final AuthProperties properties;
    private final SecurityErrorHandler errors;
    private final Clock clock;
    private final Map<String, Bucket> buckets = new HashMap<>();
    private record Bucket(long minute, int count) {}

    public AuthRateLimitFilter(AuthProperties properties, SecurityErrorHandler errors, Clock clock) {
        this.properties = properties;
        this.errors = errors;
        this.clock = clock;
    }

    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !Set.of(
                        "/delta/auth/create-account", "/delta/auth/login", "/delta/auth/refresh")
                .contains(request.getRequestURI().substring(request.getContextPath().length()));
    }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                               FilterChain chain) throws ServletException, IOException {
        if (!allow(request.getRemoteAddr())) {
            response.setHeader("Retry-After", "60");
            errors.write(request, response, HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas. Aguarde para tentar novamente.");
            return;
        }
        chain.doFilter(request, response);
    }

    private synchronized boolean allow(String address) {
        long minute = clock.instant().getEpochSecond() / 60;
        if (buckets.size() >= 10000) buckets.entrySet().removeIf(e -> e.getValue().minute() != minute);
        Bucket previous = buckets.get(address);
        if (previous == null && buckets.size() >= 10000) return false;
        int count = previous == null || previous.minute() != minute ? 0 : previous.count();
        if (count >= properties.getAttemptsPerMinute()) return false;
        buckets.put(address, new Bucket(minute, count + 1));
        return true;
    }
}
