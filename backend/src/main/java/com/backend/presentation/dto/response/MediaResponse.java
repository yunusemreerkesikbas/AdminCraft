package com.backend.presentation.dto.response;

import java.time.LocalDateTime;

import com.backend.domain.entity.Media;
import com.backend.domain.enums.MediaStatus;
import com.backend.domain.enums.StorageProvider;

/**
 * Response DTO for Media entity.
 * Follows the coding guideline: "Return Response DTOs only from controllers,
 * never return domain entities directly."
 */
public record MediaResponse(
    Long id,
    String uuid,
    String uid,
    String originalName,
    String fileName,
    String mimeType,
    String fileExtension,
    Long fileSize,
    String fileSizeFormatted,
    Integer width,
    Integer height,
    String dimensions,
    Long folderId,
    Boolean isPublic,
    Long uploadedBy,
    StorageProvider storageProvider,
    String externalUrl,
    Integer usageCount,
    MediaStatus status,
    String fileType,
    String publicUrl,
    LocalDateTime lastAccessedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static MediaResponse from(Media entity) {
    if (entity == null) {
      throw new IllegalArgumentException("Media entity cannot be null");
    }
    return new MediaResponse(
        entity.getId(),
        entity.getUuid(),
        entity.getUid(),
        entity.getOriginalName(),
        entity.getFileName(),
        entity.getMimeType(),
        entity.getFileExtension(),
        entity.getFileSize(),
        entity.getFileSizeFormatted(),
        entity.getWidth(),
        entity.getHeight(),
        entity.getDimensions(),
        entity.getFolder() != null ? entity.getFolder().getId() : null,
        entity.getIsPublic(),
        entity.getUploadedBy(),
        entity.getStorageProvider(),
        entity.getExternalUrl(),
        entity.getUsageCount(),
        entity.getStatus(),
        entity.getFileType(),
        entity.getPublicUrl(),
        entity.getLastAccessedAt(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
