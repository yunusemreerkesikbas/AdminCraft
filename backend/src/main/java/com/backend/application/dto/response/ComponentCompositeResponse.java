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
import com.backend.domain.enums.NavigationType;
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
    Long navigationNodeId,
    Long navigationLinkNodeId,
    NavigationType navigationType,
    Boolean searchBox,
    ComponentStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Map<Language, ComponentI18nDto> translations) {

  public static ComponentCompositeResponse from(
      Component component,
      String typeName,
      List<ComponentI18n> i18nList) {

    Map<Language, ComponentI18nDto> translationsMap = Optional.ofNullable(i18nList)
        .orElseGet(Collections::emptyList)
        .stream()
        .collect(Collectors.toMap(
            ComponentI18n::getLanguage,
            ComponentI18nDto::from,
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
        .navigationNodeId(component.getNavigationNodeId())
        .navigationLinkNodeId(component.getNavigationLinkNodeId())
        .navigationType(component.getNavigationType())
        .searchBox(component.getSearchBox())
        .status(component.getStatus())
        .createdAt(component.getCreatedAt())
        .updatedAt(component.getUpdatedAt())
        .translations(translationsMap)
        .build();
  }
}
