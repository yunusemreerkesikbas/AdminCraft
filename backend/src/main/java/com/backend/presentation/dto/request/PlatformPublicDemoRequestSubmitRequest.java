package com.backend.presentation.dto.request;

import com.backend.shared.constants.ValidationConstants;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PlatformPublicDemoRequestSubmitRequest(
    @NotBlank
    @Size(max = 255)
    String fullName,

    @NotBlank
    @Email
    @Size(max = 255)
    String email,

    @Size(max = 40)
    @Pattern(regexp = ValidationConstants.PHONE_GLOBAL_OPTIONAL_PATTERN, message = "validation.phone.pattern")
    String phone,

    @NotBlank
    @Size(max = 5000)
    String message,

    @NotBlank
    @Size(min = 2, max = 10)
    @Pattern(regexp = "^[a-zA-Z]{2,10}(-[a-zA-Z]{2,10})?$", message = "validation.locale.invalid")
    String locale,

    @Size(max = ValidationConstants.RECAPTCHA_TOKEN_MAX_LENGTH)
    @Pattern(regexp = "^(|[A-Za-z0-9_-]+)$", message = "validation.recaptcha.token.invalid")
    String recaptchaToken
) {

    public com.backend.application.dto.platform.PlatformDemoRequestSubmitCommand toCommand(String clientIp, String userAgent) {
        String normalizedPhone = phone != null && !phone.isBlank() ? phone.trim() : null;
        return new com.backend.application.dto.platform.PlatformDemoRequestSubmitCommand(
            fullName,
            email,
            normalizedPhone,
            message,
            locale,
            recaptchaToken != null && recaptchaToken.isBlank() ? null : recaptchaToken,
            clientIp,
            userAgent
        );
    }
}
