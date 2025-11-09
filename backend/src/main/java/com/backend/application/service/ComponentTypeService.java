package com.backend.application.service;

import com.backend.presentation.dto.request.ComponentTypeCreateRequest;
import com.backend.presentation.dto.response.ComponentTypeResponse;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public interface ComponentTypeService {
    ComponentTypeResponse createComponentType(ComponentTypeCreateRequest request, Long userId);
    ComponentTypeResponse getComponentTypeById(Long id);
    ComponentTypeResponse getComponentTypeByCode(String code);
    List<ComponentTypeResponse> getAllComponentTypes();
    List<ComponentTypeResponse> getComponentTypesByCategory(String category);
    ComponentTypeResponse updateComponentType(Long id, ComponentTypeCreateRequest request, Long userId);
    void deleteComponentType(Long id);
    JsonNode validateSchema(JsonNode schema);
}
