package com.backend.presentation.dto.response;

import java.math.BigDecimal;

import com.backend.application.dto.response.PlatformSettingsData;
import com.backend.domain.enums.TwoFactorPolicy;

public record PlatformSettingsResponse(
    String platformName,
    String defaultLanguage,
    String defaultCurrency,
    String emailFromAddress,
    String emailFromName,
    TwoFactorPolicy twoFactorPolicy,
    Boolean recaptchaEnabled,
    String recaptchaSiteKey,
    BigDecimal recaptchaThreshold
) {
    public static PlatformSettingsResponse from(PlatformSettingsData entity) {
        return new PlatformSettingsResponse(
            entity.platformName(),
            entity.defaultLanguage(),
            entity.defaultCurrency(),
            entity.emailFromAddress(),
            entity.emailFromName(),
            entity.twoFactorPolicy(),
            entity.recaptchaEnabled(),
            entity.recaptchaSiteKey(),
            entity.recaptchaThreshold()
        );
    }
}
