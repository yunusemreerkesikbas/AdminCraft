package com.backend.presentation.dto.response;

import java.time.LocalDateTime;

import com.backend.domain.entity.MediaFolder;

/**
 * Response DTO for MediaFolder entity.
 * Supports hierarchical folder structure.
 */
public record MediaFolderResponse(
    Long id,
    String uuid,
    String uid,
    String code,
    String name,
    Long parentId,
    String path,
    Integer depth,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  /**
   * Creates a MediaFolderResponse from a MediaFolder entity.
   *
   * @param entity the MediaFolder entity
   * @return MediaFolderResponse DTO
   * @throws IllegalArgumentException if entity is null
   */
  public static MediaFolderResponse from(MediaFolder entity) {
    if (entity == null) {
      throw new IllegalArgumentException("MediaFolder entity cannot be null");
    }
    return new MediaFolderResponse(
        entity.getId(),
        entity.getUuid(),
        entity.getUid(),
        entity.getCode(),
        entity.getName(),
        entity.getParent() != null ? entity.getParent().getId() : null,
        entity.getPath(),
        entity.getDepth(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
