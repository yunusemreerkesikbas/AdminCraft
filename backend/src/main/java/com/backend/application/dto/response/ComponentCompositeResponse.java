package com.backend.application.dto.response;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.response.ComponentI18nResponse;

import lombok.Builder;

@Builder
public record ComponentCompositeResponse(
    Long id,
    String uuid,
    String uid,
    Long componentTypeId,
    String componentTypeName,
    String name,
    Integer displayOrder,
    Boolean isVisible,
    String styleClasses,
    ComponentStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Map<Language, ComponentI18nResponse> translations) {

  public static ComponentCompositeResponse from(
      Component component,
      String typeName,
      List<ComponentI18n> i18nList) {

    if (component == null) {
      return null;
    }

    Map<Language, ComponentI18nResponse> translationsMap = Optional.ofNullable(i18nList)
        .orElseGet(Collections::emptyList)
        .stream()
        .collect(Collectors.toMap(
            ComponentI18n::getLanguage,
            ComponentI18nResponse::from,
            (existing, replacement) -> replacement));

    return ComponentCompositeResponse.builder()
        .id(component.getId())
        .uuid(component.getUuid())
        .uid(component.getUid())
        .componentTypeId(component.getComponentTypeId())
        .componentTypeName(typeName)
        .name(component.getName())
        .displayOrder(component.getDisplayOrder())
        .isVisible(component.getIsVisible())
        .styleClasses(component.getStyleClasses())
        .status(component.getStatus())
        .createdAt(component.getCreatedAt())
        .updatedAt(component.getUpdatedAt())
        .translations(translationsMap)
        .build();
  }
}
