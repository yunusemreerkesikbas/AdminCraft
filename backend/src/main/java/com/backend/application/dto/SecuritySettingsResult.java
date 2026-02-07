package com.backend.application.dto;

import com.backend.domain.enums.TwoFactorPolicy;

import java.math.BigDecimal;

public record SecuritySettingsResult(
    TwoFactorPolicy policy,
    String policyDescription,
    Boolean recaptchaEnabled,
    String recaptchaSiteKey,
    BigDecimal recaptchaThreshold
) {
    public static SecuritySettingsResult of(TwoFactorPolicy policy, String description) {
        return new SecuritySettingsResult(
            policy,
            description,
            false,
            null,
            new BigDecimal("0.5")
        );
    }

    public static SecuritySettingsResult of(
            TwoFactorPolicy policy,
            String description,
            Boolean recaptchaEnabled,
            String recaptchaSiteKey,
            BigDecimal recaptchaThreshold
    ) {
        return new SecuritySettingsResult(
            policy,
            description,
            recaptchaEnabled,
            recaptchaSiteKey,
            recaptchaThreshold
        );
    }
}
