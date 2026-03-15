package com.backend.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.ComponentCreateRequest;
import com.backend.application.dto.request.ComponentI18nCommand;
import com.backend.application.dto.request.CreateComponentCompositeRequest;
import com.backend.application.dto.request.UpdateComponentCompositeRequest;
import com.backend.application.dto.response.ComponentCompositeResponse;
import com.backend.application.dto.response.ComponentListItemResponse;
import com.backend.application.query.ComponentTypeQueries.GetComponentTypeByIdQuery;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.entity.ComponentType;
import com.backend.domain.entity.ResponsiveMediaSet;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.NavigationType;
import com.backend.domain.exception.BusinessRuleViolationException;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.repository.ComponentEntryI18nRepository;
import com.backend.domain.repository.ComponentEntryRepository;
import com.backend.domain.repository.ComponentI18nRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.domain.repository.NavigationNodeRepository;
import com.backend.domain.repository.ResponsiveMediaSetRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComponentServiceImpl implements ComponentService {
    private static final NavigationType DEFAULT_NAVIGATION_TYPE = NavigationType.MAINMENU;

    private final ComponentRepository componentRepository;
    private final ComponentEntryRepository componentEntryRepository;
    private final ComponentEntryI18nRepository componentEntryI18nRepository;
    private final ComponentI18nRepository componentI18nRepository;
    private final ComponentTypeService componentTypeService;
    private final ComponentTypeRepository componentTypeRepository;
    private final NavigationNodeRepository navigationNodeRepository;
    private final ResponsiveMediaSetRepository responsiveMediaSetRepository;
    private final ComponentMediaLinkSyncService componentMediaLinkSyncService;

    @Override
    @Transactional
    public Component createComponent(ComponentCreateRequest request, Long userId) {
        ComponentType componentType = componentTypeService
                .getComponentTypeById(new GetComponentTypeByIdQuery(request.componentTypeId()));

        Component component = new Component();
        component.setComponentTypeId(request.componentTypeId());
        validateUidUnique(request.uid(), null);
        component.setUid(request.uid());
        component.setName(resolveName(request.name(), request.uid()));
        component.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);
        component.setIsVisible(request.isVisible() != null ? request.isVisible() : true);
        component.setStyleClasses(request.styleClasses());
        applyNavigationBinding(component, componentType, request.navigationNodeId(),
                request.navigationType(), request.searchBox(), false);
        component.setCreatedBy(userId);
        component.setUpdatedBy(userId);

        return componentRepository.save(component);
    }

    @Override
    @Transactional(readOnly = true)
    public Component getComponentById(Long id) {
        return componentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Component", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Component, List<ComponentI18n>> getComponentWithI18n(Long id) {
        Component component = componentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Component", id));
        List<ComponentI18n> i18nList = componentI18nRepository.findByComponentId(id);

        Map<Component, List<ComponentI18n>> result = new HashMap<>();
        result.put(component, i18nList);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Component> getAllComponents() {
        return componentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Component, List<ComponentI18n>> getAllComponentsWithTranslations() {
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
    public List<ComponentListItemResponse> getAllComponentsWithTypeNames() {
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
    public Page<ComponentListItemResponse> searchComponents(Pageable pageable, String searchQuery) {
        Page<Object[]> results;

        if (searchQuery == null || searchQuery.trim().length() < 2) {
            results = componentRepository.findAllPagedWithTypeNamesAndEntryCount(pageable);
        } else {
            results = componentRepository.searchByQueryWithTypeNamesAndEntryCount(searchQuery.trim(), pageable);
        }

        return results.map(this::mapToListItemResponse);
    }

    private ComponentListItemResponse mapToListItemResponse(Object[] row) {
        Component component = (Component) row[0];
        String typeName = (String) row[1];
        Long entryCount = (Long) row[2];
        return ComponentListItemResponse.from(component, typeName, entryCount.intValue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Component> getComponentsByTypeId(Long typeId) {
        return componentRepository.findByComponentTypeId(typeId);
    }

    @Override
    @Transactional
    public Component updateComponent(Long id, ComponentCreateRequest request, Long userId) {
        Component component = componentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Component", id));

        ComponentType componentType = componentTypeService
                .getComponentTypeById(new GetComponentTypeByIdQuery(request.componentTypeId()));

        component.setComponentTypeId(request.componentTypeId());
        if (request.uid() != null) {
            validateUidUnique(request.uid(), component.getUid());
            component.setUid(request.uid());
        }
        if (request.name() != null) {
            component.setName(resolveName(request.name(), component.getUid()));
        }
        component.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : component.getDisplayOrder());
        component.setIsVisible(request.isVisible() != null ? request.isVisible() : component.getIsVisible());
        component.setStyleClasses(request.styleClasses() != null ? request.styleClasses() : component.getStyleClasses());
        applyNavigationBinding(component, componentType, request.navigationNodeId(),
                request.navigationType(), request.searchBox(), true);
        component.setUpdatedBy(userId);

        return componentRepository.save(component);
    }

    @Override
    @Transactional
    public void deleteComponent(Long id) {
        Component component = componentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Component", id));

        componentMediaLinkSyncService.removeComponentResponsiveLinks(id);
        componentRepository.delete(component);
    }

    @Override
    @Transactional
    public ComponentCompositeResponse createComposite(CreateComponentCompositeRequest request) {
        ComponentType type = componentTypeRepository.findById(request.componentTypeId())
                .orElseThrow(() -> new EntityNotFoundException("ComponentType", request.componentTypeId()));

        Component component = new Component();
        component.setComponentTypeId(request.componentTypeId());
        validateUidUnique(request.uid(), null);
        component.setUid(request.uid());
        component.setName(resolveName(request.name(), request.uid()));
        component.setDisplayOrder(request.displayOrder());
        component.setIsVisible(request.isVisible());
        component.setStyleClasses(request.styleClasses());
        applyNavigationBinding(component, type, request.navigationNodeId(),
                request.navigationType(), request.searchBox(), false);

        Component savedComponent = componentRepository.save(component);

        List<ComponentI18n> i18nList = new ArrayList<>();
        for (var entry : request.translations().entrySet()) {
            ComponentI18n i18n = new ComponentI18n();
            i18n.setComponentId(savedComponent.getId());
            i18n.setLanguage(entry.getKey());

            ComponentI18nCommand data = entry.getValue();
            if (data != null) {
                i18n.setTitle(data.title());
                i18n.setSubtitle(data.subtitle());
                i18n.setDescription(data.description());
            }

            i18nList.add(componentI18nRepository.save(i18n));
        }

        createBootstrapEntryWithDraftTranslations(savedComponent.getId(), request.translations().keySet());

        log.info("Created component with {} translations: id={}, uid={}",
                i18nList.size(), savedComponent.getId(), savedComponent.getUid());

        return ComponentCompositeResponse.from(savedComponent, type.getName(), i18nList);
    }

    @Override
    @Transactional
    public ComponentCompositeResponse updateComposite(Long id, UpdateComponentCompositeRequest request) {
        Component component = componentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Component", id));

        if (request.uid() != null) {
            validateUidUnique(request.uid(), component.getUid());
            component.setUid(request.uid());
        }
        if (request.name() != null) {
            component.setName(resolveName(request.name(), component.getUid()));
        }
        if (request.displayOrder() != null) {
            component.setDisplayOrder(request.displayOrder());
        }
        if (request.isVisible() != null) {
            component.setIsVisible(request.isVisible());
        }
        if (request.styleClasses() != null) {
            component.setStyleClasses(request.styleClasses());
        }

        ComponentType componentType = componentTypeRepository.findById(component.getComponentTypeId())
                .orElseThrow(() -> new EntityNotFoundException("ComponentType", component.getComponentTypeId()));
        applyNavigationBinding(component, componentType, request.navigationNodeId(),
                request.navigationType(), request.searchBox(), true);

        if (request.responsiveMediaId() != null) {
            ResponsiveMediaSet responsiveMedia = responsiveMediaSetRepository.findById(request.responsiveMediaId())
                    .orElseThrow(() -> new EntityNotFoundException("ResponsiveMediaSet", request.responsiveMediaId()));
            component.setResponsiveMedia(responsiveMedia);
        }

        Component savedComponent = componentRepository.save(component);

        if (request.responsiveMediaId() != null) {
            componentMediaLinkSyncService.syncComponentResponsiveLinks(savedComponent);
        }

        String typeName = componentType.getUid();

        List<ComponentI18n> i18nList;
        if (request.translations() != null && !request.translations().isEmpty()) {
            i18nList = new ArrayList<>();
            for (var entry : request.translations().entrySet()) {
                ComponentI18n i18n = componentI18nRepository
                        .findByComponentIdAndLanguage(id, entry.getKey())
                        .orElseGet(() -> {
                            ComponentI18n newI18n = new ComponentI18n();
                            newI18n.setComponentId(id);
                            newI18n.setLanguage(entry.getKey());
                            return newI18n;
                        });

                ComponentI18nCommand data = entry.getValue();
                if (data != null) {
                    if (data.title() != null) {
                        i18n.setTitle(data.title());
                    }
                    if (data.subtitle() != null) {
                        i18n.setSubtitle(data.subtitle());
                    }
                    if (data.description() != null) {
                        i18n.setDescription(data.description());
                    }
                }

                i18nList.add(componentI18nRepository.save(i18n));
            }
        } else {
            i18nList = componentI18nRepository.findByComponentId(id);
        }

        log.info("Updated component with {} translations: id={}", i18nList.size(), id);

        return ComponentCompositeResponse.from(savedComponent, typeName, i18nList);
    }

    private void createBootstrapEntryWithDraftTranslations(Long componentId, Set<Language> languages) {
        if (languages == null || languages.isEmpty()) {
            return;
        }

        ComponentEntry bootstrapEntry = new ComponentEntry();
        bootstrapEntry.setComponentId(componentId);
        bootstrapEntry.setSortOrder(0);
        bootstrapEntry.setIsVisible(true);
        bootstrapEntry.setStatus(ComponentStatus.DRAFT);
        ComponentEntry savedEntry = componentEntryRepository.save(bootstrapEntry);

        List<ComponentEntryI18n> i18nEntries = languages.stream()
                .map(language -> {
                    ComponentEntryI18n i18n = new ComponentEntryI18n();
                    i18n.setEntryId(savedEntry.getId());
                    i18n.setLanguage(language);
                    i18n.setStatus(ComponentStatus.DRAFT);
                    return i18n;
                })
                .toList();

        componentEntryI18nRepository.saveAll(i18nEntries);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ComponentCompositeResponse> getComposite(Long id) {
        return componentRepository.findById(id)
                .map(component -> {
                    String typeName = componentTypeRepository.findById(component.getComponentTypeId())
                            .map(ComponentType::getUid)
                            .orElse(null);

                    List<ComponentI18n> i18nList = componentI18nRepository.findByComponentId(id);

                    return ComponentCompositeResponse.from(component, typeName, i18nList);
                });
    }

    @Override
    @Transactional
    public Component assignResponsiveMedia(Long componentId, Long responsiveMediaId) {
        Component component = componentRepository.findById(componentId)
                .orElseThrow(() -> new EntityNotFoundException("Component", componentId));

        if (responsiveMediaId != null) {
            ResponsiveMediaSet responsiveMedia = responsiveMediaSetRepository.findById(responsiveMediaId)
                    .orElseThrow(() -> new EntityNotFoundException("ResponsiveMediaSet", responsiveMediaId));
            component.setResponsiveMedia(responsiveMedia);
            log.info("Assigned responsive media {} to component {}", responsiveMediaId, componentId);
        } else {
            component.setResponsiveMedia(null);
            log.info("Removed responsive media from component {}", componentId);
        }

        Component saved = componentRepository.save(component);
        componentMediaLinkSyncService.syncComponentResponsiveLinks(saved);
        if (saved.getResponsiveMedia() != null) {
            org.hibernate.Hibernate.initialize(saved.getResponsiveMedia());
        }

        return saved;
    }

    private void applyNavigationBinding(
            Component component,
            ComponentType componentType,
            Long requestedNavigationNodeId,
            NavigationType requestedNavigationType,
            Boolean requestedSearchBox,
            boolean partialUpdate) {
        if (componentType == null || !componentType.isNavigationAware()) {
            component.setNavigationNodeId(null);
            component.setNavigationType(null);
            component.setSearchBox(null);
            return;
        }

        Long effectiveNodeId = resolveValue(component.getNavigationNodeId(), requestedNavigationNodeId, partialUpdate);
        NavigationType effectiveType = resolveValue(component.getNavigationType(), requestedNavigationType, partialUpdate);
        Boolean effectiveSearchBox = resolveValue(component.getSearchBox(), requestedSearchBox, partialUpdate);

        if (effectiveNodeId != null) {
            validateNavigationNodeExists(effectiveNodeId);
        }

        component.setNavigationNodeId(effectiveNodeId);
        component.setNavigationType(effectiveType != null ? effectiveType : DEFAULT_NAVIGATION_TYPE);
        component.setSearchBox(effectiveSearchBox != null ? effectiveSearchBox : Boolean.FALSE);
    }

    private void validateNavigationNodeExists(Long nodeId) {
        if (navigationNodeRepository.findById(nodeId).isEmpty()) {
            throw new EntityNotFoundException("NavigationNode", nodeId);
        }
    }

    private <T> T resolveValue(T current, T requested, boolean partialUpdate) {
        if (!partialUpdate) {
            return requested;
        }
        return requested != null ? requested : current;
    }

    private void validateUidUnique(String uid, String currentUid) {
        if (uid == null || uid.isBlank()) {
            return;
        }
        if (uid.equals(currentUid)) {
            return;
        }
        if (componentRepository.existsByUid(uid)) {
            throw new BusinessRuleViolationException("component.uid.exists.simple");
        }
    }

    private String resolveName(String name, String uid) {
        if (name == null || name.trim().isEmpty()) {
            if (uid == null || uid.trim().isEmpty()) {
                throw new BusinessRuleViolationException("component.name.cannot.be.blank");
            }
            return uid.trim();
        }
        return name.trim();
    }
}
