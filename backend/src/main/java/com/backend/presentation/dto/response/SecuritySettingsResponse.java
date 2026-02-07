package com.backend.presentation.dto.response;

import com.backend.domain.enums.TwoFactorPolicy;

import java.math.BigDecimal;

public record SecuritySettingsResponse(
    TwoFactorPolicyDto twoFactor,
    RecaptchaDto recaptcha
) {
    public record TwoFactorPolicyDto(
        TwoFactorPolicy policy,
        String policyDescription
    ) {}

    public record RecaptchaDto(
        Boolean enabled,
        String siteKey,
        BigDecimal threshold
    ) {}

    public static SecuritySettingsResponse of(TwoFactorPolicy policy, String description) {
        return new SecuritySettingsResponse(
            new TwoFactorPolicyDto(policy, description),
            new RecaptchaDto(false, null, new BigDecimal("0.5"))
        );
    }

    public static SecuritySettingsResponse of(
            TwoFactorPolicy policy,
            String description,
            Boolean recaptchaEnabled,
            String recaptchaSiteKey,
            BigDecimal recaptchaThreshold
    ) {
        return new SecuritySettingsResponse(
            new TwoFactorPolicyDto(policy, description),
            new RecaptchaDto(recaptchaEnabled, recaptchaSiteKey, recaptchaThreshold)
        );
    }
}
