package com.backend.presentation.dto.response;

import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.enums.ComponentStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record EntryResponse(
    Long id,
    Integer sortOrder,
    Boolean isVisible,
    String styleClasses,
    ComponentStatus status,
    Map<String, EntryI18nResponse> translations) {
  public static EntryResponse from(ComponentEntry entry, List<ComponentEntryI18n> i18nList) {
    Map<String, EntryI18nResponse> translations = i18nList.stream()
        .collect(Collectors.toMap(
            i18n -> i18n.getLanguage().name(),
            EntryI18nResponse::from));

    return new EntryResponse(
        entry.getId(),
        entry.getSortOrder(),
        entry.getIsVisible(),
        entry.getStyleClasses(),
        entry.getStatus(),
        translations);
  }
}
