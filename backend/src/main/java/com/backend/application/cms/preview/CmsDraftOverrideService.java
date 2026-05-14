package com.backend.application.cms.preview;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.ComponentI18nUpdateCommand;
import com.backend.application.dto.request.UpdateComponentCompositeRequest;
import com.backend.domain.entity.CmsDraftOverride;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.entity.ComponentType;
import com.backend.domain.entity.PageSlot;
import com.backend.domain.entity.SlotComponent;
import com.backend.domain.enums.CmsDraftTargetType;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.NavigationType;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.repository.CmsDraftOverrideRepository;
import com.backend.domain.repository.ComponentI18nRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.domain.repository.NavigationNodeRepository;
import com.backend.domain.repository.PageSlotRepository;
import com.backend.domain.repository.ResponsiveMediaSetRepository;
import com.backend.domain.repository.SlotComponentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CmsDraftOverrideService {

    private final CmsDraftOverrideRepository draftOverrideRepository;
    private final ComponentRepository componentRepository;
    private final ComponentI18nRepository componentI18nRepository;
    private final PageSlotRepository pageSlotRepository;
    private final SlotComponentRepository slotComponentRepository;
    private final ResponsiveMediaSetRepository responsiveMediaSetRepository;
    private final ComponentTypeRepository componentTypeRepository;
    private final NavigationNodeRepository navigationNodeRepository;
    private final ObjectMapper objectMapper;

    @Transactional(transactionManager = "tenantTransactionManager")
    public void saveComponentDraft(Long componentId, UpdateComponentCompositeRequest request) {
        Component component = componentRepository.findById(componentId)
            .orElseThrow(() -> new EntityNotFoundException("Component", componentId));

        ComponentType componentType = componentTypeRepository.findById(component.getComponentTypeId())
            .orElseThrow(() -> new EntityNotFoundException("ComponentType", component.getComponentTypeId()));

        if (request.responsiveMediaId() != null) {
            responsiveMediaSetRepository.findById(request.responsiveMediaId())
                .orElseThrow(() -> new EntityNotFoundException("ResponsiveMediaSet", request.responsiveMediaId()));
        }

        Long navigationNodeId = request.navigationNodeId();
        NavigationType navigationType = request.navigationType();
        Boolean searchBox = request.searchBox();
        if (!componentType.isNavigationAware()) {
            navigationNodeId = null;
            navigationType = null;
            searchBox = null;
        } else if (navigationNodeId != null) {
            final Long nodeId = navigationNodeId;
            navigationNodeRepository.findById(nodeId)
                .orElseThrow(() -> new EntityNotFoundException("NavigationNode", nodeId));
        }

        ComponentDraftPayload componentPayload = new ComponentDraftPayload(
            request.name(),
            request.displayOrder(),
            request.isVisible(),
            request.styleClasses(),
            request.responsiveMediaId(),
            navigationNodeId,
            navigationType,
            searchBox);
        saveOverride(CmsDraftTargetType.COMPONENT, component.getId(), CmsDraftOverride.NO_LANGUAGE, componentPayload);

        if (request.translations() == null) {
            return;
        }

        for (Map.Entry<Language, ComponentI18nUpdateCommand> entry : request.translations().entrySet()) {
            ComponentI18nUpdateCommand command = entry.getValue();
            if (command == null) {
                continue;
            }
            ComponentI18nDraftPayload payload = new ComponentI18nDraftPayload(
                command.hasTitle() ? command.title() : null,
                command.hasSubtitle() ? command.subtitle() : null,
                command.hasDescription() ? command.description() : null);
            saveOverride(CmsDraftTargetType.COMPONENT_I18N, component.getId(), entry.getKey().name(), payload);
        }
    }

    @Transactional(transactionManager = "tenantTransactionManager")
    public void publishComponentDraftsForPage(Long pageId, Language language) {
        if (pageId == null || language == null) {
            return;
        }

        Set<Long> pageComponentIds = findPageComponentIds(pageId);
        if (pageComponentIds.isEmpty()) {
            return;
        }

        List<CmsDraftOverride> componentDrafts = draftOverrideRepository
            .findByTargetTypeAndTargetIdIn(CmsDraftTargetType.COMPONENT, pageComponentIds)
            .stream()
            .filter(draft -> CmsDraftOverride.NO_LANGUAGE.equals(draft.getLanguageKey()))
            .toList();
        for (CmsDraftOverride draft : componentDrafts) {
            applyComponentDraft(draft);
        }

        List<CmsDraftOverride> i18nDrafts = draftOverrideRepository
            .findByTargetTypeAndTargetIdIn(CmsDraftTargetType.COMPONENT_I18N, pageComponentIds)
            .stream()
            .filter(draft -> language.name().equals(draft.getLanguageKey()))
            .toList();
        for (CmsDraftOverride draft : i18nDrafts) {
            applyComponentI18nDraft(draft);
        }

        draftOverrideRepository.deleteAll(componentDrafts);
        draftOverrideRepository.deleteAll(i18nDrafts);
    }

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public Optional<ComponentDraftPayload> findComponentDraft(Long componentId) {
        return draftOverrideRepository.findByTargetTypeAndTargetIdAndLanguageKey(
                CmsDraftTargetType.COMPONENT, componentId, CmsDraftOverride.NO_LANGUAGE)
            .map(draft -> readPayload(draft.getPayload(), ComponentDraftPayload.class));
    }

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public Map<Long, ComponentDraftPayload> findComponentDrafts(Collection<Long> componentIds) {
        if (componentIds == null || componentIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, ComponentDraftPayload> result = new LinkedHashMap<>();
        draftOverrideRepository.findByTargetTypeAndTargetIdIn(CmsDraftTargetType.COMPONENT, componentIds)
            .stream()
            .filter(draft -> CmsDraftOverride.NO_LANGUAGE.equals(draft.getLanguageKey()))
            .forEach(draft -> result.put(draft.getTargetId(), readPayload(draft.getPayload(), ComponentDraftPayload.class)));
        return result;
    }

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public Optional<ComponentI18nDraftPayload> findComponentI18nDraft(Long componentId, Language language) {
        if (language == null) {
            return Optional.empty();
        }
        return draftOverrideRepository.findByTargetTypeAndTargetIdAndLanguageKey(
                CmsDraftTargetType.COMPONENT_I18N, componentId, language.name())
            .map(draft -> readPayload(draft.getPayload(), ComponentI18nDraftPayload.class));
    }

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public Map<String, ComponentI18nDraftPayload> findComponentI18nDrafts(Collection<Long> componentIds, Language language) {
        if (language == null) {
            return Map.of();
        }
        if (componentIds == null || componentIds.isEmpty()) {
            return Map.of();
        }
        Map<String, ComponentI18nDraftPayload> result = new LinkedHashMap<>();
        draftOverrideRepository.findByTargetTypeAndTargetIdIn(CmsDraftTargetType.COMPONENT_I18N, componentIds)
            .stream()
            .filter(draft -> language.name().equals(draft.getLanguageKey()))
            .forEach(draft -> result.put(i18nKey(draft.getTargetId(), language),
                readPayload(draft.getPayload(), ComponentI18nDraftPayload.class)));
        return result;
    }

    public String i18nKey(Long componentId, Language language) {
        return componentId + ":" + language.name();
    }

    public Component apply(Component component, ComponentDraftPayload draft) {
        if (draft == null) {
            return component;
        }
        if (draft.name() != null) {
            component.setName(draft.name());
        }
        if (draft.displayOrder() != null) {
            component.setDisplayOrder(draft.displayOrder());
        }
        if (draft.isVisible() != null) {
            component.setIsVisible(draft.isVisible());
        }
        if (draft.styleClasses() != null) {
            component.setStyleClasses(draft.styleClasses());
        }
        if (draft.navigationNodeId() != null) {
            component.setNavigationNodeId(draft.navigationNodeId());
        }
        if (draft.navigationType() != null) {
            component.setNavigationType(draft.navigationType());
        }
        if (draft.searchBox() != null) {
            component.setSearchBox(draft.searchBox());
        }
        if (draft.responsiveMediaId() != null) {
            component.setResponsiveMedia(responsiveMediaSetRepository.findById(draft.responsiveMediaId())
                .orElseThrow(() -> new EntityNotFoundException("ResponsiveMediaSet", draft.responsiveMediaId())));
        }
        return component;
    }

    public ComponentI18n apply(ComponentI18n i18n, ComponentI18nDraftPayload draft) {
        if (draft == null) {
            return i18n;
        }
        if (draft.title() != null) {
            i18n.setTitle(draft.title());
        }
        if (draft.subtitle() != null) {
            i18n.setSubtitle(draft.subtitle());
        }
        if (draft.description() != null) {
            i18n.setDescription(draft.description());
        }
        return i18n;
    }

    private void applyComponentDraft(CmsDraftOverride draft) {
        componentRepository.findById(draft.getTargetId()).ifPresent(component -> {
            apply(component, readPayload(draft.getPayload(), ComponentDraftPayload.class));
            componentRepository.save(component);
        });
    }

    private void applyComponentI18nDraft(CmsDraftOverride draft) {
        Language language = Language.valueOf(draft.getLanguageKey());
        ComponentI18n i18n = componentI18nRepository
            .findByComponentIdAndLanguage(draft.getTargetId(), language)
            .orElseGet(() -> {
                ComponentI18n created = new ComponentI18n();
                created.setComponentId(draft.getTargetId());
                created.setLanguage(language);
                created.setStatus(ComponentStatus.PUBLISHED);
                return created;
            });
        apply(i18n, readPayload(draft.getPayload(), ComponentI18nDraftPayload.class));
        i18n.setStatus(ComponentStatus.PUBLISHED);
        componentI18nRepository.save(i18n);
    }

    private Set<Long> findPageComponentIds(Long pageId) {
        List<Long> slotIds = pageSlotRepository.findByPageId(pageId)
            .stream()
            .filter(slot -> !Boolean.TRUE.equals(slot.getIsShared()))
            .map(PageSlot::getId)
            .filter(id -> id != null)
            .toList();

        if (slotIds.isEmpty()) {
            return Set.of();
        }

        return slotComponentRepository.findBySlotIdIn(slotIds)
            .stream()
            .filter(slotComponent -> Boolean.TRUE.equals(slotComponent.getIsVisible()))
            .map(SlotComponent::getComponentId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
    }

    private void saveOverride(CmsDraftTargetType targetType, Long targetId, String languageKey, Object payload) {
        CmsDraftOverride draft = draftOverrideRepository
            .findByTargetTypeAndTargetIdAndLanguageKey(targetType, targetId, languageKey)
            .orElseGet(CmsDraftOverride::new);
        draft.setTargetType(targetType);
        draft.setTargetId(targetId);
        draft.setLanguageKey(languageKey);
        draft.setPayload(writePayload(payload));
        draftOverrideRepository.save(draft);
    }

    private String writePayload(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize CMS draft override", ex);
        }
    }

    private <T> T readPayload(String payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse CMS draft override", ex);
        }
    }
}
