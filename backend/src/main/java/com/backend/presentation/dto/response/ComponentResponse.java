package com.backend.presentation.dto.response;

import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.ComponentType;

import java.util.List;
import java.util.Map;

public record ComponentResponse(
    Long id,
    Long tenantId,
    ComponentType type,
    String key,
    String uid,
    String uuid,
    ComponentStatus status,
    boolean visible,
    Integer sortOrder,
    String styleClasses,
    Map<String, ComponentTranslationDto> translations,
    List<NavbarItemEntryResponse> items) {
  public static record ComponentTranslationDto(
      String title,
      String subtitle,
      String data) {
  }
}
