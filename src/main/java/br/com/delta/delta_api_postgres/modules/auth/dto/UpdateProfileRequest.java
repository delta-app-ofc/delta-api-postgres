package br.com.delta.delta_api_postgres.modules.auth.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Locale;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 15) String phone,
        @NotNull @Past LocalDate birthDate
) {
    public UpdateProfileRequest {
        name = name == null ? null : name.strip();
        email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
        phone = phone == null || phone.isBlank() ? null : phone.strip();
    }
}
