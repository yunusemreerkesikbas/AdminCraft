package com.backend.application.dto.request;

import com.backend.shared.constants.ValidationConstants;

import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * Request DTO for patching Site Technical Settings.
 * All fields are optional - only non-null fields will be updated.
 */
public record SiteTechnicalPatchRequest(
        @Size(max = ValidationConstants.ROBOTS_TXT_MAX_LENGTH) String robotsTxt,
        Boolean sitemapEnabled,
        Boolean indexingEnabled,

        Boolean cookieConsentEnabled,

        Map<String, String> cookieConsentTexts) {

    public boolean hasAnyUpdate() {
        return robotsTxt != null ||
                sitemapEnabled != null ||
                indexingEnabled != null ||
                cookieConsentEnabled != null ||
                cookieConsentTexts != null;
    }
}
