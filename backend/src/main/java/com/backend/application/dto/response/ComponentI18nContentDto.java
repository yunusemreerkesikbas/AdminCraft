package com.backend.application.dto.response;

import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.enums.ComponentStatus;

public record ComponentI18nContentDto(
    String title,
    String subtitle,
    String description,
    ComponentStatus status
) {
  public static ComponentI18nContentDto from(ComponentI18n entity) {
    if (entity == null) {
      throw new IllegalArgumentException("ComponentI18n entity cannot be null");
    }
    return new ComponentI18nContentDto(
        entity.getTitle(),
        entity.getSubtitle(),
        entity.getDescription(),
        entity.getStatus());
  }
}
