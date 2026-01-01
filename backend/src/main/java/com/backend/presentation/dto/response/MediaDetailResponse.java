package com.backend.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.backend.domain.entity.Media;
import com.backend.domain.entity.MediaContainer;
import com.backend.domain.entity.MediaI18n;
import com.backend.domain.enums.MediaStatus;
import com.backend.domain.enums.StorageProvider;

/**
 * Detailed response DTO for Media entity.
 * Includes folder, container (with variants), and i18n translations.
 */
public record MediaDetailResponse(
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
    MediaFolderResponse folder,
    Boolean isPublic,
    Long uploadedBy,
    StorageProvider storageProvider,
    String externalUrl,
    Integer usageCount,
    MediaStatus status,
    String fileType,
    String publicUrl,
    MediaContainerResponse container,
    Map<String, MediaI18nResponse> translations,
    LocalDateTime lastAccessedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  /**
   * Creates a MediaDetailResponse from Media entity with container and i18n data.
   *
   * @param entity    the Media entity
   * @param container the MediaContainer (nullable)
   * @param i18nList  the list of MediaI18n entries
   * @return MediaDetailResponse DTO
   * @throws IllegalArgumentException if entity is null
   */
  public static MediaDetailResponse from(Media entity, MediaContainer container, List<MediaI18n> i18nList) {
    if (entity == null) {
      throw new IllegalArgumentException("Media entity cannot be null");
    }

    Map<String, MediaI18nResponse> translationsMap = i18nList != null
        ? i18nList.stream()
            .collect(Collectors.toMap(
                i18n -> i18n.getLanguage().name(),
                MediaI18nResponse::from,
                (existing, replacement) -> existing))
        : Map.of();

    return new MediaDetailResponse(
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
        entity.getFolder() != null ? MediaFolderResponse.from(entity.getFolder()) : null,
        entity.getIsPublic(),
        entity.getUploadedBy(),
        entity.getStorageProvider(),
        entity.getExternalUrl(),
        entity.getUsageCount(),
        entity.getStatus(),
        entity.getFileType(),
        entity.getPublicUrl(),
        container != null ? MediaContainerResponse.from(container) : null,
        translationsMap,
        entity.getLastAccessedAt(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  /**
   * Creates a MediaDetailResponse from Media entity without container and i18n.
   *
   * @param entity the Media entity
   * @return MediaDetailResponse DTO
   */
  public static MediaDetailResponse from(Media entity) {
    return from(entity, null, null);
  }
}
