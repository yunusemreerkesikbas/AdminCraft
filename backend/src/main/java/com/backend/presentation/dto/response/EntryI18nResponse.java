package com.backend.presentation.dto.response;

import java.util.Map;

import com.backend.domain.entity.ComponentEntryI18n;

public record EntryI18nResponse(
    String title,
    String description,
    Map<String, Object> dynamicFields) {
  public static EntryI18nResponse from(ComponentEntryI18n i18n) {
    return new EntryI18nResponse(
        i18n.getTitle(),
        i18n.getDescription(),
        null);
  }
}
