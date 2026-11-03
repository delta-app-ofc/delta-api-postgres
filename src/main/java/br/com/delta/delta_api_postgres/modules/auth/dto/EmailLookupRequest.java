package br.com.delta.delta_api_postgres.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Locale;

public record EmailLookupRequest(@NotBlank @Email @Size(max = 255) String email) {
    public EmailLookupRequest {
        email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }
}
