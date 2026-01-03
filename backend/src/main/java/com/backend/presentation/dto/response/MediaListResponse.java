package com.backend.presentation.dto.response;

import java.time.LocalDateTime;

import com.backend.domain.entity.Media;
import com.backend.domain.enums.MediaStatus;

/**
 * Lightweight response DTO for Media list views.
 * Contains only essential fields for grid/list display.
 * Use MediaDetailResponse for full media information.
 */
public record MediaListResponse(
    Long id,
    String uid,
    String originalName,
    String mimeType,
    String fileSizeFormatted,
    String dimensions,
    String fileType,
    String publicUrl,
    MediaStatus status,
    LocalDateTime createdAt) {

  public static MediaListResponse from(Media entity) {
    if (entity == null) {
      throw new IllegalArgumentException("Media entity cannot be null");
    }
    return new MediaListResponse(
        entity.getId(),
        entity.getUid(),
        entity.getOriginalName(),
        entity.getMimeType(),
        entity.getFileSizeFormatted(),
        entity.getDimensions(),
        entity.getFileType(),
        entity.getPublicUrl(),
        entity.getStatus(),
        entity.getCreatedAt());
  }
}
