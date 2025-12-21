package com.backend.application.dto.response;

import com.backend.domain.entity.NavigationEntryI18n;
import com.backend.domain.enums.Language;

import lombok.Builder;

@Builder
public record NavigationEntryI18nResponse(
    Long id,
    Long entryId,
    Language language,
    String linkName) {
  public static NavigationEntryI18nResponse from(NavigationEntryI18n entity) {
    if (entity == null) {
      return null;
    }
    return NavigationEntryI18nResponse.builder()
        .id(entity.getId())
        .entryId(entity.getEntryId())
        .language(entity.getLanguage())
        .linkName(entity.getLinkName())
        .build();
  }
}
