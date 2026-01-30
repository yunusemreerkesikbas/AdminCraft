package com.backend.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO for Site Technical Settings.
 * Used in the Technical tab of Site Dashboard.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)

public record SiteTechnicalResponse(
        SearchEngineDto searchEngine,
        ScriptsDto scripts,
        CookieConsentDto cookieConsent) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SearchEngineDto(
            String robotsTxt,
            Boolean sitemapEnabled,
            Boolean indexingEnabled,
            VerificationDto verification) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VerificationDto(
            String google,
            String bing,
            String yandex) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ScriptsDto(
            String headScripts,
            String bodyStartScripts,
            String bodyEndScripts) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CookieConsentDto(
            Boolean enabled,
            String text) {
    }

    /**
     * Builder for creating SiteTechnicalResponse.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SearchEngineDto searchEngine;
        private ScriptsDto scripts;
        private CookieConsentDto cookieConsent;

        public Builder searchEngine(SearchEngineDto searchEngine) {
            this.searchEngine = searchEngine;
            return this;
        }

        public Builder scripts(ScriptsDto scripts) {
            this.scripts = scripts;
            return this;
        }

        public Builder cookieConsent(CookieConsentDto cookieConsent) {
            this.cookieConsent = cookieConsent;
            return this;
        }

        public SiteTechnicalResponse build() {
            return new SiteTechnicalResponse(searchEngine, scripts, cookieConsent);
        }
    }
}
