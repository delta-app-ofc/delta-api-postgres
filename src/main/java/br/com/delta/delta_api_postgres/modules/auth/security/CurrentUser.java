package br.com.delta.delta_api_postgres.modules.auth.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    public Integer id() {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken auth) {
            try { return Integer.valueOf(auth.getToken().getSubject()); }
            catch (NumberFormatException ignored) { /* Deny malformed identities. */ }
        }
        throw new AccessDeniedException("Identidade inválida.");
    }

    public void requireUser(Integer userId) {
        if (!id().equals(userId)) throw new AccessDeniedException("Acesso não permitido.");
    }
}
