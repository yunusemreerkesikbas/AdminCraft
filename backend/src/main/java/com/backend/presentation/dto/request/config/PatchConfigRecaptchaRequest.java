package com.backend.presentation.dto.request.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PatchConfigRecaptchaRequest(
        Boolean recaptchaEnabled,

        @Pattern(regexp = "^[A-Za-z0-9_-]{40}$", message = "validation.recaptcha.siteKey.invalid")
        String recaptchaSiteKey,

        @Pattern(regexp = "^[A-Za-z0-9_-]{40}$", message = "validation.recaptcha.secretKey.invalid")
        String recaptchaSecretKey,

        @NotBlank(message = "validation.reason.required")
        @Size(min = 5, max = 500, message = "validation.reason.size")
        String reason
) {
}
