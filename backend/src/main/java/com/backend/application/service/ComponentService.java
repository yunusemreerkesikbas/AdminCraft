package com.backend.application.service;

import com.backend.application.command.ComponentCommands.*;
import com.backend.application.query.ComponentQueries.*;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentI18n;
import java.util.List;
import java.util.Map;

public interface ComponentService {
    Component createComponent(CreateComponentCommand command);
    Component getComponentById(GetComponentByIdQuery query);
    Map<Component, List<ComponentI18n>> getComponentWithI18n(GetComponentWithI18nQuery query);
    List<Component> getAllComponents(GetAllComponentsQuery query);
    Map<Component, List<ComponentI18n>> getAllComponentsWithTranslations(GetAllComponentsWithTranslationsQuery query);
    List<Component> getComponentsByTypeId(GetComponentsByTypeIdQuery query);
    Component updateComponent(UpdateComponentCommand command);
    void deleteComponent(DeleteComponentCommand command);
}
