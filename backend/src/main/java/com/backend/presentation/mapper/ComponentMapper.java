package com.backend.presentation.mapper;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentTranslation;
import com.backend.presentation.dto.response.ComponentResponse;

public class ComponentMapper {

  public static ComponentResponse toResponse(
      Component component,
      ComponentTranslation tr,
      ComponentTranslation en) {
    ComponentResponse.ComponentTranslationDto trDto = (tr == null)
        ? new ComponentResponse.ComponentTranslationDto("", "", "")
        : new ComponentResponse.ComponentTranslationDto(
            safe(tr.getTitle()), safe(tr.getSubtitle()), safe(tr.getData()));

    ComponentResponse.ComponentTranslationDto enDto = (en == null)
        ? new ComponentResponse.ComponentTranslationDto("", "", "")
        : new ComponentResponse.ComponentTranslationDto(
            safe(en.getTitle()), safe(en.getSubtitle()), safe(en.getData()));

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

  private static String safe(String value) {
    return value == null ? "" : value;
  }
}
