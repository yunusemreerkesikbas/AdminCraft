package com.backend.presentation.dto.response;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentI18n;
import java.util.List;
import java.util.stream.Collectors;

public record ComponentListResponse(
    ComponentResponse component,
    List<ComponentI18nResponse> translations
) {
    public static ComponentListResponse from(Component component, List<ComponentI18n> i18nList) {
        if (component == null) {
            throw new IllegalArgumentException("Component entity cannot be null");
        }
        ComponentResponse componentResponse = ComponentResponse.from(component);
        List<ComponentI18nResponse> translations = i18nList != null
            ? i18nList.stream().map(ComponentI18nResponse::from).collect(Collectors.toList())
            : List.of();
        return new ComponentListResponse(componentResponse, translations);
    }
}
