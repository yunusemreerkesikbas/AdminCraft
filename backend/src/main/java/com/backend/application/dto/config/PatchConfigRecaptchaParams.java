package com.backend.application.dto.config;

public record PatchConfigRecaptchaParams(
    Boolean recaptchaEnabled,
    String recaptchaSiteKey,
    String recaptchaSecretKey,
    String reason
) {
}

