package com.backend.application.dto.response;

import com.backend.domain.entity.NavigationNodeI18n;
import com.backend.domain.enums.Language;

import lombok.Builder;

@Builder
public record NavigationNodeI18nResponse(
    Long id,
    Long nodeId,
    Language language,
    String title) {
  public static NavigationNodeI18nResponse from(NavigationNodeI18n entity) {
    if (entity == null) {
      return null;
    }
    return NavigationNodeI18nResponse.builder()
        .id(entity.getId())
        .nodeId(entity.getNodeId())
        .language(entity.getLanguage())
        .title(entity.getTitle())
        .build();
  }
}
