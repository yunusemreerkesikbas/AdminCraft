package com.backend.application.dto.response;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;

public record ComponentEntryCompositeResponse(
    Long id,
    String uuid,
    Long componentId,
    Integer sortOrder,
    Boolean isVisible,
    String styleClasses,
    ComponentStatus status,
    ResponsiveMediaResponse responsiveMedia,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Map<Language, ComponentEntryI18nDto> translations) {

  public static ComponentEntryCompositeResponse from(
      ComponentEntry entry,
      List<ComponentEntryI18n> i18nList,
      Map<Language, Map<String, Object>> customFieldsByLanguage) {

    if (entry == null) {
      return null;
    }

    Map<Language, ComponentEntryI18nDto> translationsMap = Optional.ofNullable(i18nList)
        .orElseGet(Collections::emptyList)
        .stream()
        .collect(Collectors.toMap(
            ComponentEntryI18n::getLanguage,
            i18n -> ComponentEntryI18nDto.from(i18n,
                Optional.ofNullable(customFieldsByLanguage)
                    .map(fields -> fields.get(i18n.getLanguage()))
                    .orElseGet(Collections::emptyMap)),
            (existing, replacement) -> replacement));

    return new ComponentEntryCompositeResponse(
        entry.getId(),
        entry.getUuid(),
        entry.getComponentId(),
        entry.getSortOrder(),
        entry.getIsVisible(),
        entry.getStyleClasses(),
        entry.getStatus(),
        entry.getResponsiveMedia() != null ? ResponsiveMediaResponse.from(entry.getResponsiveMedia()) : null,
        entry.getCreatedAt(),
        entry.getUpdatedAt(),
        translationsMap);
  }
}
