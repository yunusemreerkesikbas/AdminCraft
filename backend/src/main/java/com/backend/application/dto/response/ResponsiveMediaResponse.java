package com.backend.application.dto.response;

import java.time.LocalDateTime;

import com.backend.domain.entity.Media;
import com.backend.domain.entity.ResponsiveMediaSet;

/**
 * Response DTO for ResponsiveMediaSet.
 */
public record ResponsiveMediaResponse(
    Long id,
    String uuid,
    String uid,
    String code,
    MediaSummary desktopMedia,
    MediaSummary mobileMedia,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  /**
   * Summary of a media item for responsive sets.
   */
  public record MediaSummary(
      Long id,
      String uid,
      String fileName,
      String originalName,
      String mimeType,
      String publicUrl,
      Integer width,
      Integer height,
      String fileSizeFormatted) {
    public static MediaSummary from(Media media) {
      if (media == null) {
        return null;
      }
      return new MediaSummary(
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

  /**
   * Factory method to create response from entity.
   */
  public static ResponsiveMediaResponse from(ResponsiveMediaSet entity) {
    return new ResponsiveMediaResponse(
        entity.getId(),
        entity.getUuid(),
        entity.getUid(),
        entity.getCode(),
        MediaSummary.from(entity.getDesktopMedia()),
        MediaSummary.from(entity.getMobileMedia()),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
