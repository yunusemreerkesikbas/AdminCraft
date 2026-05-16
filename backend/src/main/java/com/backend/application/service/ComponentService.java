package com.backend.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.application.dto.request.ComponentCreateRequest;
import com.backend.application.dto.request.CreateComponentCompositeRequest;
import com.backend.application.dto.request.UpdateComponentCompositeRequest;
import com.backend.application.dto.response.BulkDeleteResultResponse;
import com.backend.application.dto.response.ComponentCompositeResponse;
import com.backend.application.dto.response.ComponentListItemResponse;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentI18n;

public interface ComponentService {
    Component createComponent(ComponentCreateRequest request, Long userId);

    Component getComponentById(Long id);

    Component getComponentByUid(String uid);

    Map<Component, List<ComponentI18n>> getComponentWithI18n(Long id);

    Map<Component, List<ComponentI18n>> getComponentWithI18nByUid(String uid);

    List<Component> getAllComponents();

    Map<Component, List<ComponentI18n>> getAllComponentsWithTranslations();

    List<ComponentListItemResponse> getAllComponentsWithTypeNames();

    Page<ComponentListItemResponse> searchComponents(Pageable pageable, String searchQuery);

    List<Component> getComponentsByTypeId(Long typeId);

    Component updateComponent(Long id, ComponentCreateRequest request, Long userId);

    void deleteComponent(Long id);

    BulkDeleteResultResponse bulkDeleteComponents(List<Long> ids);

    ComponentCompositeResponse createComposite(CreateComponentCompositeRequest request);

    ComponentCompositeResponse updateComposite(Long id, UpdateComponentCompositeRequest request);

    ComponentCompositeResponse updateDraftComposite(Long id, UpdateComponentCompositeRequest request);

    Optional<ComponentCompositeResponse> getComposite(Long id);

    Component assignResponsiveMedia(Long componentId, Long responsiveMediaId);
}
