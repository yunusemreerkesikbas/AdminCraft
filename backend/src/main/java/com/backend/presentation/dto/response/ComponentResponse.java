package com.backend.presentation.dto.response;

import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.ComponentType;

import java.util.Map;

public record ComponentResponse(
    Long id,
    Long tenantId,
    ComponentType type,
    String key,
    ComponentStatus status,
    boolean visible,
    Integer sortOrder,
    String styleClasses,
    Map<String, ComponentTranslationDto> translations) {
  public static record ComponentTranslationDto(
      String title,
      String subtitle,
      String data) {
  }
}
