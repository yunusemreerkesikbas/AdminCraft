package com.backend.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * Response DTO for Site Technical Settings.
 * Used in the Technical tab of Site Dashboard.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)

public record SiteTechnicalResponse(
        SearchEngineDto searchEngine,
        CookieConsentDto cookieConsent) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SearchEngineDto(
            String robotsTxt,
            Boolean sitemapEnabled,
            Boolean indexingEnabled) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CookieConsentDto(
            Boolean enabled,
            Map<String, String> texts) {
    }

    /**
     * Builder for creating SiteTechnicalResponse.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SearchEngineDto searchEngine;
        private CookieConsentDto cookieConsent;

        public Builder searchEngine(SearchEngineDto searchEngine) {
            this.searchEngine = searchEngine;
            return this;
        }

        public Builder cookieConsent(CookieConsentDto cookieConsent) {
            this.cookieConsent = cookieConsent;
            return this;
        }

        public SiteTechnicalResponse build() {
            return new SiteTechnicalResponse(searchEngine, cookieConsent);
        }
    }
}
