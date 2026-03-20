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

        Boolean cookieConsentEnabled,

        @Size(max = ValidationConstants.COOKIE_CONSENT_TEXT_MAX_LENGTH) String cookieConsentText) {

    public boolean hasAnyUpdate() {
        return robotsTxt != null ||
                sitemapEnabled != null ||
                indexingEnabled != null ||
                cookieConsentEnabled != null ||
                cookieConsentText != null;
    }
}
