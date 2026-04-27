package com.backend.presentation.dto.request;

import com.backend.application.dto.contact.ContactRequestSubmitCommand;
import com.backend.shared.constants.ValidationConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PublicContactRequestSubmitRequest(
    @NotBlank
    @Size(max = 255)
    String fullName,

    @NotBlank
    @Size(max = 255)
    String subject,

    @NotBlank
    @Size(max = 5000)
    String message,

    @NotBlank
    @Size(min = 2, max = 10)
    @Pattern(regexp = "^[a-zA-Z]{2,10}(-[a-zA-Z]{2,10})?$", message = "validation.locale.invalid")
    String locale,

    @Size(max = ValidationConstants.RECAPTCHA_TOKEN_MAX_LENGTH)
    @Pattern(regexp = ValidationConstants.RECAPTCHA_TOKEN_OPTIONAL_PATTERN, message = "validation.recaptcha.token.invalid")
    String recaptchaToken
) {

    public ContactRequestSubmitCommand toCommand(String clientIp, String userAgent) {
        return new ContactRequestSubmitCommand(
                fullName,
                subject,
                message,
                locale,
                recaptchaToken != null && recaptchaToken.isBlank() ? null : recaptchaToken,
                clientIp,
                userAgent);
    }
}
