package com.backend.presentation.dto.request;

import com.backend.domain.enums.TwoFactorPolicy;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * Request DTO for updating tenant security settings.
 */
public record UpdateSecuritySettingsRequest(
    @NotNull(message = "validation.2fa.policy.required")
    TwoFactorPolicy twoFactorPolicy,

    Boolean recaptchaEnabled,

    @Pattern(regexp = "^[A-Za-z0-9_-]{40}$", message = "validation.recaptcha.siteKey.invalid")
    String recaptchaSiteKey,

    @Pattern(regexp = "^[A-Za-z0-9_-]{40}$", message = "validation.recaptcha.secretKey.invalid")
    String recaptchaSecretKey,

    @DecimalMin(value = "0.0", message = "validation.recaptcha.threshold.min")
    @DecimalMax(value = "1.0", message = "validation.recaptcha.threshold.max")
    BigDecimal recaptchaThreshold
) {}
