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
import com.backend.presentation.dto.response.ComponentEntryI18nResponse;

import lombok.Builder;

@Builder
public record ComponentEntryCompositeResponse(
    Long id,
    String uuid,
    Long componentId,
    Integer sortOrder,
    Boolean isVisible,
    String styleClasses,
    ComponentStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Map<Language, ComponentEntryI18nResponse> translations) {

  public static ComponentEntryCompositeResponse from(
      ComponentEntry entry,
      List<ComponentEntryI18n> i18nList,
      java.util.function.Function<String, Map<String, Object>> customDataParser) {

    if (entry == null) {
      return null;
    }

    Map<Language, ComponentEntryI18nResponse> translationsMap = Optional.ofNullable(i18nList)
        .orElseGet(Collections::emptyList)
        .stream()
        .collect(Collectors.toMap(
            ComponentEntryI18n::getLanguage,
            i18n -> ComponentEntryI18nResponse.from(i18n, customDataParser.apply(i18n.getCustomData())),
            (existing, replacement) -> replacement));

    return ComponentEntryCompositeResponse.builder()
        .id(entry.getId())
        .uuid(entry.getUuid())
        .componentId(entry.getComponentId())
        .sortOrder(entry.getSortOrder())
        .isVisible(entry.getIsVisible())
        .styleClasses(entry.getStyleClasses())
        .status(entry.getStatus())
        .createdAt(entry.getCreatedAt())
        .updatedAt(entry.getUpdatedAt())
        .translations(translationsMap)
        .build();
  }
}
