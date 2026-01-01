package com.backend.presentation.dto.response;

import java.time.LocalDateTime;

import com.backend.domain.entity.MediaI18n;
import com.backend.domain.enums.Language;

/**
 * Response DTO for MediaI18n entity.
 * Represents localized metadata for media files.
 */
public record MediaI18nResponse(
    Long id,
    String uuid,
    String uid,
    Long mediaId,
    Language language,
    String altText,
    String title,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  /**
   * Creates a MediaI18nResponse from a MediaI18n entity.
   *
   * @param entity the MediaI18n entity
   * @return MediaI18nResponse DTO
   * @throws IllegalArgumentException if entity is null
   */
  public static MediaI18nResponse from(MediaI18n entity) {
    if (entity == null) {
      throw new IllegalArgumentException("MediaI18n entity cannot be null");
    }
    return new MediaI18nResponse(
        entity.getId(),
        entity.getUuid(),
        entity.getUid(),
        entity.getMedia() != null ? entity.getMedia().getId() : null,
        entity.getLanguage(),
        entity.getAltText(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
