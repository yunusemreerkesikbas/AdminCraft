package com.backend.application.dto;

import java.math.BigDecimal;

public record PublicTenantConfigResult(
    RecaptchaConfig recaptcha
) {
    public record RecaptchaConfig(
        Boolean enabled,
        String siteKey,
        BigDecimal threshold
    ) {}

    public static PublicTenantConfigResult of(
            Boolean recaptchaEnabled,
            String recaptchaSiteKey,
            BigDecimal recaptchaThreshold
    ) {
        return new PublicTenantConfigResult(
            new RecaptchaConfig(recaptchaEnabled, recaptchaSiteKey, recaptchaThreshold)
        );
    }

    public static PublicTenantConfigResult disabled() {
        return new PublicTenantConfigResult(
            new RecaptchaConfig(false, null, new BigDecimal("0.5"))
        );
    }
}
