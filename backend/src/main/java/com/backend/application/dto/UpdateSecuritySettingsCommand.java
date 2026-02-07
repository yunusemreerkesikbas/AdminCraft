package com.backend.application.dto;

import com.backend.domain.enums.TwoFactorPolicy;

import java.math.BigDecimal;

public record UpdateSecuritySettingsCommand(
    TwoFactorPolicy twoFactorPolicy,
    Boolean recaptchaEnabled,
    String recaptchaSiteKey,
    String recaptchaSecretKey,
    BigDecimal recaptchaThreshold
) {
}
