package com.backend.presentation.dto.request;

import com.backend.domain.enums.TwoFactorPolicy;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating tenant security settings.
 */
public record UpdateSecuritySettingsRequest(
    @NotNull(message = "validation.2fa.policy.required")
    TwoFactorPolicy twoFactorPolicy
) {}
