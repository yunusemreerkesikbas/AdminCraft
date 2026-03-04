package com.backend.application.dto;

import com.backend.domain.enums.TwoFactorPolicy;

public record SecuritySettingsResult(
    TwoFactorPolicy policy,
    String policyDescription
) {
    public static SecuritySettingsResult of(TwoFactorPolicy policy, String description) {
        return new SecuritySettingsResult(policy, description);
    }
}
