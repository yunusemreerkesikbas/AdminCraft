package com.backend.application.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.command.ComponentCommands.CreateComponentCommand;
import com.backend.application.command.ComponentCommands.DeleteComponentCommand;
import com.backend.application.command.ComponentCommands.UpdateComponentCommand;
import com.backend.application.query.ComponentQueries.GetAllComponentsQuery;
import com.backend.application.query.ComponentQueries.GetAllComponentsWithTranslationsQuery;
import com.backend.application.query.ComponentQueries.GetComponentByIdQuery;
import com.backend.application.query.ComponentQueries.GetComponentWithI18nQuery;
import com.backend.application.query.ComponentQueries.GetComponentsByTypeIdQuery;
import com.backend.application.query.ComponentTypeQueries.GetComponentTypeByIdQuery;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.repository.ComponentI18nRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.infrastructure.util.UuidUidGenerator;
import com.backend.presentation.dto.response.ComponentListItemResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComponentServiceImpl implements ComponentService {

    private final ComponentRepository componentRepository;
    private final ComponentI18nRepository componentI18nRepository;
    private final ComponentTypeService componentTypeService;

    @Override
    @Transactional
    public Component createComponent(CreateComponentCommand command) {
        componentTypeService.getComponentTypeById(new GetComponentTypeByIdQuery(command.componentTypeId()));

        Component component = new Component();
        component.setComponentTypeId(command.componentTypeId());
        component.setName(command.name());
        component.setDisplayOrder(command.displayOrder() != null ? command.displayOrder() : 0);
        component.setIsVisible(command.isVisible() != null ? command.isVisible() : true);
        component.setStyleClasses(command.styleClasses());
        component.setStatus(command.status() != null ? command.status() : ComponentStatus.DRAFT);
        component.setCreatedBy(command.userId());
        component.setUpdatedBy(command.userId());

        return componentRepository.save(component);
    }

    @Override
    @Transactional(readOnly = true)
    public Component getComponentById(GetComponentByIdQuery query) {
        return componentRepository.findById(query.id())
                .orElseThrow(() -> new IllegalArgumentException("Component not found with id: " + query.id()));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Component, List<ComponentI18n>> getComponentWithI18n(GetComponentWithI18nQuery query) {
        Component component = componentRepository.findById(query.id())
                .orElseThrow(() -> new IllegalArgumentException("Component not found with id: " + query.id()));
        List<ComponentI18n> i18nList = componentI18nRepository.findByComponentId(query.id());

        Map<Component, List<ComponentI18n>> result = new HashMap<>();
        result.put(component, i18nList);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Component> getAllComponents(GetAllComponentsQuery query) {
        return componentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Component, List<ComponentI18n>> getAllComponentsWithTranslations(
            GetAllComponentsWithTranslationsQuery query) {
        List<Component> components = componentRepository.findAll();
        List<ComponentI18n> allTranslations = componentI18nRepository.findAll();

        Map<Long, List<ComponentI18n>> translationsByComponent = allTranslations.stream()
                .collect(Collectors.groupingBy(ComponentI18n::getComponentId));

        Map<Component, List<ComponentI18n>> result = new HashMap<>();
        for (Component component : components) {
            result.put(component, translationsByComponent.getOrDefault(component.getId(), List.of()));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComponentListItemResponse> getAllComponentsWithTypeNames(GetAllComponentsQuery query) {
        List<Object[]> results = componentRepository.findAllWithTypeNamesAndEntryCount();

        return results.stream()
                .map(row -> {
                    Component component = (Component) row[0];
                    String typeName = (String) row[1];
                    Long entryCount = (Long) row[2];
                    return ComponentListItemResponse.from(component, typeName, entryCount.intValue());
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Component> getComponentsByTypeId(GetComponentsByTypeIdQuery query) {
        return componentRepository.findByComponentTypeId(query.typeId());
    }

    @Override
    @Transactional
    public Component updateComponent(UpdateComponentCommand command) {
        Component component = componentRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Component not found with id: " + command.id()));

        componentTypeService.getComponentTypeById(new GetComponentTypeByIdQuery(command.componentTypeId()));

        component.setComponentTypeId(command.componentTypeId());
        component.setName(command.name());
        component
                .setDisplayOrder(command.displayOrder() != null ? command.displayOrder() : component.getDisplayOrder());
        component.setIsVisible(command.isVisible() != null ? command.isVisible() : component.getIsVisible());
        component
                .setStyleClasses(command.styleClasses() != null ? command.styleClasses() : component.getStyleClasses());
        if (command.status() != null) {
            component.setStatus(command.status());
        }
        component.setUpdatedBy(command.userId());

        return componentRepository.save(component);
    }

    @Override
    @Transactional
    public void deleteComponent(DeleteComponentCommand command) {
        Component component = componentRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Component not found with id: " + command.id()));
        componentRepository.delete(component);
    }

    private String generateUniqueUid() {
        String uid;
        do {
            uid = UuidUidGenerator.generateUid();
        } while (componentRepository.existsByUid(uid));
        return uid;
    }
}
