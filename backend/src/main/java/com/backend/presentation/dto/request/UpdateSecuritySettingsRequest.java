package com.backend.presentation.dto.request;

import com.backend.domain.enums.TwoFactorPolicy;

import jakarta.validation.constraints.NotNull;

public record UpdateSecuritySettingsRequest(
    @NotNull(message = "validation.twoFactorPolicy.required")
    TwoFactorPolicy twoFactorPolicy
) {
}
