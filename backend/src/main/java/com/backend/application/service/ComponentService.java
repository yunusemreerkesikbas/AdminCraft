package com.backend.application.service;

import com.backend.presentation.dto.request.ComponentCreateRequest;
import com.backend.presentation.dto.response.ComponentResponse;
import com.backend.presentation.dto.response.ComponentDetailResponse;
import com.backend.presentation.dto.response.ComponentListResponse;
import java.util.List;

public interface ComponentService {
    ComponentResponse createComponent(ComponentCreateRequest request, Long userId);
    ComponentResponse getComponentById(Long id);
    ComponentDetailResponse getComponentWithI18n(Long id);
    List<ComponentResponse> getAllComponents();
    List<ComponentListResponse> getAllComponentsWithTranslations();
    List<ComponentResponse> getComponentsByTypeId(Long typeId);
    ComponentResponse updateComponent(Long id, ComponentCreateRequest request, Long userId);
    void deleteComponent(Long id);
}
