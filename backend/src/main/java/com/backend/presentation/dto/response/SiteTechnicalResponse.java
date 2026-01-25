package com.backend.presentation.dto.response;

/**
 * Response DTO for Site Technical Settings.
 * Used in the Technical tab of Site Dashboard.
 */
public record SiteTechnicalResponse(
    DomainDto domain,
    SearchEngineDto searchEngine,
    ScriptsDto scripts,
    CookieConsentDto cookieConsent
) {

    public record DomainDto(
        String subdomain,
        String platformDomain,
        String fullUrl,
        String customDomain,
        Boolean sslEnabled
    ) {}

    public record SearchEngineDto(
        String robotsTxt,
        Boolean sitemapEnabled,
        Boolean indexingEnabled,
        VerificationDto verification
    ) {}

    public record VerificationDto(
        String google,
        String bing,
        String yandex
    ) {}

    public record ScriptsDto(
        String headScripts,
        String bodyStartScripts,
        String bodyEndScripts
    ) {}

    public record CookieConsentDto(
        Boolean enabled,
        String text
    ) {}

    /**
     * Builder for creating SiteTechnicalResponse.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DomainDto domain;
        private SearchEngineDto searchEngine;
        private ScriptsDto scripts;
        private CookieConsentDto cookieConsent;

        public Builder domain(DomainDto domain) {
            this.domain = domain;
            return this;
        }

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
            return new SiteTechnicalResponse(domain, searchEngine, scripts, cookieConsent);
        }
    }
}
