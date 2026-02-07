package com.backend.presentation.dto.request;

import com.backend.shared.constants.ValidationConstants;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(
        @NotBlank(message = "validation.email.required")
        @Email(message = "validation.email.invalid")
        String email,

        @Size(max = ValidationConstants.RECAPTCHA_TOKEN_MAX_LENGTH, message = "validation.recaptcha.token.size")
        @Pattern(regexp = ValidationConstants.RECAPTCHA_TOKEN_PATTERN, message = "validation.recaptcha.token.invalid")
        String recaptchaToken
) {}
