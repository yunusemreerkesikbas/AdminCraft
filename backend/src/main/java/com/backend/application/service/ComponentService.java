package com.backend.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.application.command.ComponentCommands.CreateComponentCommand;
import com.backend.application.command.ComponentCommands.DeleteComponentCommand;
import com.backend.application.command.ComponentCommands.UpdateComponentCommand;
import com.backend.application.dto.request.CreateComponentCompositeRequest;
import com.backend.application.dto.request.UpdateComponentCompositeRequest;
import com.backend.application.dto.response.ComponentCompositeResponse;
import com.backend.application.query.ComponentQueries.GetAllComponentsQuery;
import com.backend.application.query.ComponentQueries.GetAllComponentsWithTranslationsQuery;
import com.backend.application.query.ComponentQueries.GetComponentByIdQuery;
import com.backend.application.query.ComponentQueries.GetComponentWithI18nQuery;
import com.backend.application.query.ComponentQueries.GetComponentsByTypeIdQuery;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentI18n;
import com.backend.presentation.dto.response.ComponentListItemResponse;

public interface ComponentService {
    Component createComponent(CreateComponentCommand command);

    Component getComponentById(GetComponentByIdQuery query);

    Map<Component, List<ComponentI18n>> getComponentWithI18n(GetComponentWithI18nQuery query);

    List<Component> getAllComponents(GetAllComponentsQuery query);

    Map<Component, List<ComponentI18n>> getAllComponentsWithTranslations(GetAllComponentsWithTranslationsQuery query);

    List<ComponentListItemResponse> getAllComponentsWithTypeNames(GetAllComponentsQuery query);

    Page<ComponentListItemResponse> searchComponents(Pageable pageable, String searchQuery);

    List<Component> getComponentsByTypeId(GetComponentsByTypeIdQuery query);

    Component updateComponent(UpdateComponentCommand command);

    void deleteComponent(DeleteComponentCommand command);

    ComponentCompositeResponse createComposite(CreateComponentCompositeRequest request);

    ComponentCompositeResponse updateComposite(Long id, UpdateComponentCompositeRequest request);

    Optional<ComponentCompositeResponse> getComposite(Long id);

    /**
     * Assign or update responsive media (Desktop/Mobile images) for a component.
     * 
     * @param componentId       Component ID
     * @param responsiveMediaId ResponsiveMediaSet ID (null to remove)
     * @return Updated component
     */
    Component assignResponsiveMedia(Long componentId, Long responsiveMediaId);
}
