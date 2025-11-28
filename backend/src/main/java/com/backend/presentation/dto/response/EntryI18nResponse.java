package com.backend.presentation.dto.response;

import com.backend.domain.entity.ComponentEntryI18n;
import java.util.Map;

public record EntryI18nResponse(
    String title,
    String description,
    String imageUrl,
    String buttonText,
    String buttonUrl,
    Map<String, Object> dynamicFields) {
  public static EntryI18nResponse from(ComponentEntryI18n i18n) {
    return new EntryI18nResponse(
        i18n.getTitle(),
        i18n.getDescription(),
        i18n.getImageUrl(),
        i18n.getButtonText(),
        i18n.getButtonUrl(),
        null);
  }
}
