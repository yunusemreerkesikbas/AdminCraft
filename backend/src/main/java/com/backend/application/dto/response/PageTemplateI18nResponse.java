package com.backend.application.dto.response;

import com.backend.domain.entity.PageTemplateI18n;
import com.backend.domain.enums.Language;

import lombok.Builder;

@Builder
public record PageTemplateI18nResponse(
    Long id,
    Long templateId,
    Language language,
    String name,
    String description) {
  public static PageTemplateI18nResponse from(PageTemplateI18n entity) {
    if (entity == null) {
      return null;
    }
    return PageTemplateI18nResponse.builder()
        .id(entity.getId())
        .templateId(entity.getTemplateId())
        .language(entity.getLanguage())
        .name(entity.getName())
        .description(entity.getDescription())
        .build();
  }
}
