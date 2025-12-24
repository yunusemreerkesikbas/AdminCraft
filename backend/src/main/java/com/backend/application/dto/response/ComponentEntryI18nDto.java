package com.backend.application.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;

public record ComponentEntryI18nDto(
    Long id,
    String uuid,
    String uid,
    Long entryId,
    Language language,
    String title,
    String description,
    ComponentStatus status,
    LocalDateTime publishedAt,
    LocalDateTime updatedAt,
    Map<String, Object> customFields) {
  public static ComponentEntryI18nDto from(ComponentEntryI18n entity, Map<String, Object> customFields) {
    if (entity == null) {
      throw new IllegalArgumentException("ComponentEntryI18n entity cannot be null");
    }
    return new ComponentEntryI18nDto(
        entity.getId(),
        entity.getUuid(),
        entity.getUid(),
        entity.getEntryId(),
        entity.getLanguage(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getStatus(),
        entity.getPublishedAt(),
        entity.getUpdatedAt(),
        customFields);
  }
}
