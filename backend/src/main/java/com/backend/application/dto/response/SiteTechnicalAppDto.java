package com.backend.application.dto.response;

/**
 * Application layer DTO for Site Technical Settings.
 * Replaces the dependency on presentation layer SiteTechnicalResponse.
 */
public record SiteTechnicalAppDto(
    SearchEngineAppDto searchEngine,
    CookieConsentAppDto cookieConsent) {

  public record SearchEngineAppDto(
      String robotsTxt,
      Boolean sitemapEnabled,
      Boolean indexingEnabled) {
  }

  public record CookieConsentAppDto(
      Boolean enabled,
      String text) {
  }
}
