package com.backend.application.dto;

import java.util.List;
import java.util.Map;

import com.backend.domain.entity.Media;
import com.backend.domain.enums.RobotsMetaTag;

/**
 * Application layer DTOs for Site Settings.
 * Replaces dependencies on presentation layer DTOs.
 */
public class SiteSettingsAppDto {

  public record MediaSummaryDto(
      Long id,
      String uid,
      String fileName,
      String originalName,
      String mimeType,
      String publicUrl,
      Integer width,
      Integer height,
      String fileSizeFormatted) {

    public static MediaSummaryDto from(Media media) {
      if (media == null) {
        return null;
      }
      return new MediaSummaryDto(
          media.getId(),
          media.getUid(),
          media.getFileName(),
          media.getOriginalName(),
          media.getMimeType(),
          media.getPublicUrl(),
          media.getWidth(),
          media.getHeight(),
          media.getFileSizeFormatted());
    }
  }

  public record SiteSettingsAppGlobalDto(
      String contactEmail,
      String contactPhone,
      String whatsappPhone,
      String canonicalBaseUrl,
      RobotsMetaTag robots,
      AddressDto address,
      SocialDto social,
      String logoMediaUid,
      String logoDarkMediaUid,
      MediaSummaryDto logoMedia,
      MediaSummaryDto logoDarkMedia) {

    public record AddressDto(
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String country,
        String mapEmbedUrl) {
    }

    public record SocialDto(
        String facebook,
        String instagram,
        String x,
        String linkedin,
        String youtube,
        String tiktok) {
    }
  }

  public record SiteSettingsAppI18nDto(
      String siteName,
      String tagline,
      SeoDto seo,
      String footerText,
      String headerTopbarText) {

    public record SeoDto(
        String title,
        String description,
        List<String> keywords,
        String ogTitle,
        String ogDescription,
        String twitterCard) {
    }
  }

  public record SiteSettingsAppResponseDto(
      SiteSettingsAppGlobalDto global,
      Map<String, SiteSettingsAppI18nDto> languages) {
  }
}
