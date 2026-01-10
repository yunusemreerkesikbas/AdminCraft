package com.backend.application.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.application.command.ComponentTypeCommands.CreateComponentTypeCommand;
import com.backend.application.command.ComponentTypeCommands.DeleteComponentTypeCommand;
import com.backend.application.command.ComponentTypeCommands.UpdateComponentTypeCommand;
import com.backend.application.query.ComponentTypeQueries.GetAllComponentTypesQuery;
import com.backend.application.query.ComponentTypeQueries.GetComponentTypeByIdQuery;
import com.backend.application.query.ComponentTypeQueries.GetComponentTypesByCategoryQuery;
import com.backend.domain.entity.ComponentType;

public interface ComponentTypeService {
  ComponentType createComponentType(CreateComponentTypeCommand command);

  ComponentType getComponentTypeById(GetComponentTypeByIdQuery query);

  List<ComponentType> getAllComponentTypes(GetAllComponentTypesQuery query);

  Page<ComponentType> searchComponentTypes(Pageable pageable, String searchQuery);

  List<ComponentType> getComponentTypesByCategory(GetComponentTypesByCategoryQuery query);

  ComponentType updateComponentType(UpdateComponentTypeCommand command);

  void deleteComponentType(DeleteComponentTypeCommand command);
}
