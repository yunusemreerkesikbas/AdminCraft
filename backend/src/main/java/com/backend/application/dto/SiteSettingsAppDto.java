package com.backend.application.dto;

import java.util.Map;

/**
 * Application layer DTOs for Site Settings.
 * Replaces dependencies on presentation layer DTOs.
 */
public class SiteSettingsAppDto {

  public record SiteSettingsAppGlobalDto(
      String contactEmail,
      String contactPhone,
      String whatsappPhone,
      String canonicalBaseUrl,
      String robots) {
  }

  public record SiteSettingsAppI18nDto(
      String siteName,
      String tagline,
      String seo,
      String footerText,
      String headerTopbarText,
      String addressLocalized) {
  }

  public record SiteSettingsAppResponseDto(
      SiteSettingsAppGlobalDto global,
      Map<String, SiteSettingsAppI18nDto> languages) {
  }
}
