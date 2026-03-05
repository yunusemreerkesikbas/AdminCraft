package com.backend.presentation.dto.response;

import com.backend.domain.enums.TwoFactorPolicy;

public record SecuritySettingsResponse(
    TwoFactorPolicyDto twoFactor
) {
    public record TwoFactorPolicyDto(
        TwoFactorPolicy policy,
        String policyDescription
    ) {}

    public static SecuritySettingsResponse of(TwoFactorPolicy policy, String description) {
        return new SecuritySettingsResponse(new TwoFactorPolicyDto(policy, description));
    }
}
