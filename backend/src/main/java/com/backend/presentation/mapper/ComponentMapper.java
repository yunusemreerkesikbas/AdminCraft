package com.backend.presentation.mapper;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentTranslation;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.response.ComponentResponse;
import com.backend.presentation.dto.response.SiteComponentResponse;

import java.util.HashMap;
import java.util.Map;

public class ComponentMapper {

  public static ComponentResponse toResponse(
      Component component,
      Map<Language, ComponentTranslation> translationsByLang) {
    Map<String, ComponentResponse.ComponentTranslationDto> map = new HashMap<>();
    if (translationsByLang != null) {
      for (Map.Entry<Language, ComponentTranslation> e : translationsByLang.entrySet()) {
        ComponentTranslation t = e.getValue();
        if (t == null)
          continue;
        String code = e.getKey().name().toLowerCase();
        map.put(code, new ComponentResponse.ComponentTranslationDto(
            safe(t.getTitle()), safe(t.getSubtitle()), safe(t.getData())));
      }
    }

    return new ComponentResponse(
        component.getId(),
        component.getTenantId(),
        component.getType(),
        component.getKey(),
        component.getUid(),
        component.getUuid(),
        component.getStatus(),
        component.isVisible(),
        component.getSortOrder(),
        component.getStyleClasses(),
        map,
        java.util.List.of());
  }

  public static SiteComponentResponse toSiteResponse(
      Component component,
      ComponentTranslation translation) {

    SiteComponentResponse.SiteComponentTranslationDto translationDto = translation == null
        ? null
        : new SiteComponentResponse.SiteComponentTranslationDto(
            safe(translation.getTitle()),
            safe(translation.getSubtitle()),
            safe(translation.getData()));

    return new SiteComponentResponse(
        component.getId(),
        component.getType(),
        component.getKey(),
        component.getSortOrder(),
        translationDto);
  }

  private static String safe(String value) {
    return value == null ? "" : value;
  }
}
