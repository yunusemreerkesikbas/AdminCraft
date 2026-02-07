package com.backend.presentation.dto.request;

import com.backend.shared.constants.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email is required") @Email(message = "Please provide a valid email address") String email,

        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters long") String password,
        @Pattern(regexp = ValidationConstants.SUBDOMAIN_PATTERN, message = "Subdomain must contain only lowercase letters, numbers, and hyphens") String subdomain,
        @Size(max = 128, message = "validation.device.fingerprint.size")
        String deviceFingerprint,

        @Size(max = ValidationConstants.RECAPTCHA_TOKEN_MAX_LENGTH, message = "validation.recaptcha.token.size")
        @Pattern(regexp = ValidationConstants.RECAPTCHA_TOKEN_PATTERN, message = "validation.recaptcha.token.invalid")
        String recaptchaToken) {
    public LoginRequest {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
    }
}