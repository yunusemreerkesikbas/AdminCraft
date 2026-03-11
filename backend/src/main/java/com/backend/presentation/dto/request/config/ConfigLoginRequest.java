package com.backend.presentation.dto.request.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfigLoginRequest(
        @NotBlank(message = "validation.email.required")
        @Email(message = "validation.email.invalid")
        @Size(max = 255, message = "validation.email.size")
        String email,

        @NotBlank(message = "validation.password.required")
        @Size(max = 255, message = "validation.password.size")
        String password,

        Long tenantId,

        @Size(max = 50, message = "validation.subdomain.size")
        String subdomain
) {
}
