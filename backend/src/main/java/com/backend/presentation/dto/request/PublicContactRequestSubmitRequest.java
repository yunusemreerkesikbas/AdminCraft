package com.backend.presentation.dto.request;

import com.backend.application.dto.contact.ContactRequestSubmitCommand;
import com.backend.shared.constants.ValidationConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PublicContactRequestSubmitRequest(
    @NotBlank
    @Size(max = ValidationConstants.CONTACT_FULL_NAME_MAX_LENGTH)
    String fullName,

    @NotBlank
    @Size(max = ValidationConstants.CONTACT_SUBJECT_MAX_LENGTH)
    String subject,

    @NotBlank
    @Size(max = ValidationConstants.CONTACT_MESSAGE_MAX_LENGTH)
    String message,

    @NotBlank
    @Size(min = ValidationConstants.CONTACT_LOCALE_MIN_LENGTH, max = ValidationConstants.CONTACT_LOCALE_MAX_LENGTH)
    @Pattern(regexp = ValidationConstants.CONTACT_LOCALE_PATTERN, message = "validation.locale.invalid")
    String locale,

    @Size(max = ValidationConstants.RECAPTCHA_TOKEN_MAX_LENGTH)
    @Pattern(regexp = ValidationConstants.RECAPTCHA_TOKEN_OPTIONAL_PATTERN, message = "validation.recaptcha.token.invalid")
    String recaptchaToken
) {

    public PublicContactRequestSubmitRequest {
        fullName = trim(fullName);
        subject = trim(subject);
        message = trim(message);
        locale = trim(locale);
        recaptchaToken = normalizeRecaptcha(recaptchaToken);
    }

    public ContactRequestSubmitCommand toCommand(String clientIp, String userAgent) {
        return new ContactRequestSubmitCommand(
                fullName,
                subject,
                message,
                locale,
                recaptchaToken,
                clientIp,
                userAgent);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String normalizeRecaptcha(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
