package com.backend.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.backend.domain.entity.MediaContainer;
import com.backend.domain.entity.MediaContainerItem;

/**
 * Response DTO for MediaContainer entity.
 * Groups master media with its format variants (SAP Hybris pattern).
 */
public record MediaContainerResponse(
    Long id,
    String uuid,
    String uid,
    String code,
    Long masterMediaId,
    MediaResponse masterMedia,
    List<MediaVariantResponse> variants,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  /**
   * Nested record for media format variants.
   */
  public record MediaVariantResponse(
      Long formatId,
      String formatCode,
      String formatName,
      Long mediaId,
      String mediaUrl,
      Integer width,
      Integer height) {
    public static MediaVariantResponse from(MediaContainerItem item) {
      if (item == null) {
        throw new IllegalArgumentException("MediaContainerItem cannot be null");
      }
      return new MediaVariantResponse(
          item.getFormat() != null ? item.getFormat().getId() : null,
          item.getFormat() != null ? item.getFormat().getCode() : null,
          item.getFormat() != null ? item.getFormat().getName() : null,
          item.getMedia() != null ? item.getMedia().getId() : null,
          item.getMedia() != null ? item.getMedia().getPublicUrl() : null,
          item.getMedia() != null ? item.getMedia().getWidth() : null,
          item.getMedia() != null ? item.getMedia().getHeight() : null);
    }
  }

  /**
   * Creates a MediaContainerResponse from a MediaContainer entity.
   *
   * @param entity the MediaContainer entity
   * @return MediaContainerResponse DTO
   * @throws IllegalArgumentException if entity is null
   */
  public static MediaContainerResponse from(MediaContainer entity) {
    if (entity == null) {
      throw new IllegalArgumentException("MediaContainer entity cannot be null");
    }

    List<MediaVariantResponse> variantResponses = entity.getItems() != null
        ? entity.getItems().stream()
            .map(MediaVariantResponse::from)
            .toList()
        : List.of();

    return new MediaContainerResponse(
        entity.getId(),
        entity.getUuid(),
        entity.getUid(),
        entity.getCode(),
        entity.getMasterMedia() != null ? entity.getMasterMedia().getId() : null,
        entity.getMasterMedia() != null ? MediaResponse.from(entity.getMasterMedia()) : null,
        variantResponses,
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
