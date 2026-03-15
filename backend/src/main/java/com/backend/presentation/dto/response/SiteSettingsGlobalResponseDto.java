package com.backend.presentation.dto.response;

import com.backend.domain.enums.RobotsMetaTag;

public record SiteSettingsGlobalResponseDto(
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
  }
}
