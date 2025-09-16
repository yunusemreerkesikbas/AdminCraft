package com.backend.presentation.dto.response;

import com.backend.domain.enums.ComponentType;

public record SiteComponentResponse(
    Long id,
    ComponentType type,
    String key,
    Integer sortOrder,
    SiteComponentTranslationDto translation) {

  public static record SiteComponentTranslationDto(
      String title,
      String subtitle,
      String data) {
  }
}