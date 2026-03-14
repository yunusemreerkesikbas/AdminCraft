package com.backend.presentation.dto.request;

import com.backend.shared.constants.ValidationConstants;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SetInitialPasswordRequest(
        @NotBlank(message = "validation.token.required")
        String token,

        @NotBlank(message = "validation.password.required")
        @Size(min = 8, max = 128, message = "validation.password.size")
        @Pattern(regexp = ValidationConstants.PASSWORD_PATTERN, message = "validation.password.pattern")
        String password,

        @NotBlank(message = "validation.password.confirm.required")
        String confirmPassword,

        @Size(max = ValidationConstants.RECAPTCHA_TOKEN_MAX_LENGTH, message = "validation.recaptcha.token.size")
        @Pattern(regexp = ValidationConstants.RECAPTCHA_TOKEN_PATTERN, message = "validation.recaptcha.token.invalid")
        String recaptchaToken
) {
    @AssertTrue(message = "validation.password.mismatch")
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}
