package com.backend.application.dto.response;

import java.math.BigDecimal;

import com.backend.domain.enums.TwoFactorPolicy;

public record PlatformSettingsData(
        String platformName,
        String defaultLanguage,
        String defaultCurrency,
        String emailFromAddress,
        String emailFromName,
        TwoFactorPolicy twoFactorPolicy,
        Boolean recaptchaEnabled,
        String recaptchaSiteKey,
        BigDecimal recaptchaThreshold) {
}
