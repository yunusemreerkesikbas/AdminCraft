package com.backend.presentation.dto.response;

import java.time.LocalDateTime;

import com.backend.domain.entity.MediaFormat;
import com.backend.domain.enums.CropMode;

/**
 * Response DTO for MediaFormat entity.
 * Represents format definitions for image variants.
 */
public record MediaFormatResponse(
    Long id,
    String uuid,
    String uid,
    String code,
    String name,
    Integer width,
    Integer height,
    Integer quality,
    CropMode cropMode,
    Boolean isSystem,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  /**
   * Creates a MediaFormatResponse from a MediaFormat entity.
   *
   * @param entity the MediaFormat entity
   * @return MediaFormatResponse DTO
   * @throws IllegalArgumentException if entity is null
   */
  public static MediaFormatResponse from(MediaFormat entity) {
    if (entity == null) {
      throw new IllegalArgumentException("MediaFormat entity cannot be null");
    }
    return new MediaFormatResponse(
        entity.getId(),
        entity.getUuid(),
        entity.getUid(),
        entity.getCode(),
        entity.getName(),
        entity.getWidth(),
        entity.getHeight(),
        entity.getQuality(),
        entity.getCropMode(),
        entity.getIsSystem(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
