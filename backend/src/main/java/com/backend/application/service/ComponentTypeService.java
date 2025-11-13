package com.backend.application.service;

import com.backend.application.command.ComponentTypeCommands.*;
import com.backend.application.query.ComponentTypeQueries.*;
import com.backend.domain.entity.ComponentType;
import java.util.List;

public interface ComponentTypeService {
    ComponentType createComponentType(CreateComponentTypeCommand command);
    ComponentType getComponentTypeById(GetComponentTypeByIdQuery query);
    ComponentType getComponentTypeByCode(GetComponentTypeByCodeQuery query);
    List<ComponentType> getAllComponentTypes(GetAllComponentTypesQuery query);
    List<ComponentType> getComponentTypesByCategory(GetComponentTypesByCategoryQuery query);
    ComponentType updateComponentType(UpdateComponentTypeCommand command);
    void deleteComponentType(DeleteComponentTypeCommand command);
}
