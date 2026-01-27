package com.backend.application.dto.response;

/**
 * Application layer DTO for Site Technical Settings.
 * Replaces the dependency on presentation layer SiteTechnicalResponse.
 */
public record SiteTechnicalAppDto(
    DomainAppDto domain,
    SearchEngineAppDto searchEngine,
    ScriptsAppDto scripts,
    CookieConsentAppDto cookieConsent) {
  public record DomainAppDto(
      String subdomain,
      String platformDomain,
      String fullUrl,
      String customDomain,
      Boolean sslEnabled) {
  }

  public record SearchEngineAppDto(
      String robotsTxt,
      Boolean sitemapEnabled,
      Boolean indexingEnabled,
      VerificationAppDto verification) {
  }

  public record VerificationAppDto(
      String google,
      String bing,
      String yandex) {
  }

  public record ScriptsAppDto(
      String head,
      String bodyStart,
      String bodyEnd) {
  }

  public record CookieConsentAppDto(
      Boolean enabled,
      String text) {
  }
}
