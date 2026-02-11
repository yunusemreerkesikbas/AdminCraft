package com.backend.application.dto.request;

import java.math.BigDecimal;

import com.backend.domain.enums.TwoFactorPolicy;
import com.backend.shared.validation.RecaptchaKeysValid;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RecaptchaKeysValid
public record PatchPlatformSettingsRequest(
    @Size(max = 100) String platformName,
    @Size(min = 2, max = 10)
    @Pattern(regexp = "^[A-Za-z-]{2,10}$")
    String defaultLanguage,
    @Size(min = 3, max = 3)
    @Pattern(regexp = "^[A-Za-z]{3}$")
    String defaultCurrency,
    @Email @Size(max = 255) String emailFromAddress,
    @Size(max = 100) String emailFromName,
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
