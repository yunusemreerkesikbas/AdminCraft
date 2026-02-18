package com.backend.application.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.application.query.ComponentTypeQueries.GetAllComponentTypesQuery;
import com.backend.application.query.ComponentTypeQueries.GetComponentTypeByIdQuery;
import com.backend.application.query.ComponentTypeQueries.GetComponentTypesByCategoryQuery;
import com.backend.domain.entity.ComponentType;
import com.backend.domain.enums.ComponentNavigationProfile;

public interface ComponentTypeService {
  ComponentType createComponentType(
      String name,
      String category,
      ComponentNavigationProfile navigationProfile,
      Long userId);

  ComponentType getComponentTypeById(GetComponentTypeByIdQuery query);

  List<ComponentType> getAllComponentTypes(GetAllComponentTypesQuery query);

  Page<ComponentType> searchComponentTypes(Pageable pageable, String searchQuery);

  List<ComponentType> getComponentTypesByCategory(GetComponentTypesByCategoryQuery query);

  ComponentType updateComponentType(
      Long id,
      String name,
      String category,
      ComponentNavigationProfile navigationProfile,
      Long userId);

  void deleteComponentType(Long id);
}
