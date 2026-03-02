package com.backend.application.dto.request;

import com.backend.shared.constants.ValidationConstants;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for patching Site Technical Settings.
 * All fields are optional - only non-null fields will be updated.
 */
public record SiteTechnicalPatchRequest(
        @Size(max = ValidationConstants.ROBOTS_TXT_MAX_LENGTH) String robotsTxt,
        Boolean sitemapEnabled,
        Boolean indexingEnabled,

        @Size(max = ValidationConstants.VERIFICATION_CODE_MAX_LENGTH, message = "Google verification code must not exceed {max} characters") String googleVerification,

        @Size(max = ValidationConstants.VERIFICATION_CODE_MAX_LENGTH, message = "Bing verification code must not exceed {max} characters") String bingVerification,

        @Size(max = ValidationConstants.VERIFICATION_CODE_MAX_LENGTH, message = "Yandex verification code must not exceed {max} characters") String yandexVerification,

        Boolean cookieConsentEnabled,

        @Size(max = ValidationConstants.COOKIE_CONSENT_TEXT_MAX_LENGTH) String cookieConsentText) {

    public boolean hasAnyUpdate() {
        return robotsTxt != null ||
                sitemapEnabled != null ||
                indexingEnabled != null ||
                googleVerification != null ||
                bingVerification != null ||
                yandexVerification != null ||
                cookieConsentEnabled != null ||
                cookieConsentText != null;
    }
}
