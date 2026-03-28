package com.backend.presentation.dto.request.config;

import jakarta.validation.constraints.NotBlank;

public record ConfigRefreshTokenRequest(
        @NotBlank(message = "validation.token.required")
        String refreshToken
) {
}
