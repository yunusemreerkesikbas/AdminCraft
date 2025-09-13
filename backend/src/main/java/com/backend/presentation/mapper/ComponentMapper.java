package com.backend.presentation.mapper;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentTranslation;
import com.backend.presentation.dto.response.ComponentResponse;

public class ComponentMapper {

  public static ComponentResponse toResponse(
      Component component,
      ComponentTranslation tr,
      ComponentTranslation en) {
    ComponentResponse.ComponentTranslationDto trDto = tr == null ? null
        : new ComponentResponse.ComponentTranslationDto(
            tr.getTitle(), tr.getSubtitle(), tr.getData());

    ComponentResponse.ComponentTranslationDto enDto = en == null ? null
        : new ComponentResponse.ComponentTranslationDto(
            en.getTitle(), en.getSubtitle(), en.getData());

    return new ComponentResponse(
        component.getId(),
        component.getTenantId(),
        component.getType(),
        component.getKey(),
        component.getStatus(),
        component.isVisible(),
        component.getSortOrder(),
        trDto,
        enDto);
  }
}
