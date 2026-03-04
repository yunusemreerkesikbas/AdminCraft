package com.backend.application.dto;

import com.backend.domain.enums.TwoFactorPolicy;

public record UpdateSecuritySettingsCommand(
    TwoFactorPolicy twoFactorPolicy
) {
}
