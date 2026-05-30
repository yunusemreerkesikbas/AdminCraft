package com.backend.application.cms.preview;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.ComponentI18nUpdateCommand;
import com.backend.application.dto.request.EntryI18nUpdateCommand;
import com.backend.application.dto.request.UpdateComponentEntryCompositeRequest;
import com.backend.application.dto.request.UpdateComponentCompositeRequest;
import com.backend.application.service.ComponentMediaLinkSyncService;
import com.backend.application.service.SiteActivityPublisher;
import com.backend.domain.entity.CmsDraftOverride;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.entity.ComponentType;
import com.backend.domain.entity.NavigationNode;
import com.backend.domain.entity.PageSlot;
import com.backend.domain.entity.ResponsiveMediaSet;
import com.backend.domain.entity.SlotComponent;
import com.backend.domain.enums.ActivityAction;
import com.backend.domain.enums.CmsDraftTargetType;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.NavigationType;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.repository.CmsDraftOverrideRepository;
import com.backend.domain.repository.ComponentEntryI18nRepository;
import com.backend.domain.repository.ComponentEntryRepository;
import com.backend.domain.repository.ComponentI18nRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.domain.repository.NavigationNodeRepository;
import com.backend.domain.repository.PageSlotRepository;
import com.backend.domain.repository.ResponsiveMediaSetRepository;
import com.backend.domain.repository.SlotComponentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmsDraftOverrideService {

    private final CmsDraftOverrideRepository draftOverrideRepository;
    private final ComponentRepository componentRepository;
    private final ComponentI18nRepository componentI18nRepository;
    private final ComponentEntryRepository componentEntryRepository;
    private final ComponentEntryI18nRepository componentEntryI18nRepository;
    private final PageSlotRepository pageSlotRepository;
    private final SlotComponentRepository slotComponentRepository;
    private final ResponsiveMediaSetRepository responsiveMediaSetRepository;
    private final ComponentTypeRepository componentTypeRepository;
    private final NavigationNodeRepository navigationNodeRepository;
    private final ComponentMediaLinkSyncService componentMediaLinkSyncService;
    private final SiteActivityPublisher activityPublisher;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    @Transactional(transactionManager = "tenantTransactionManager")
    public void saveComponentDraft(Long componentId, UpdateComponentCompositeRequest request) {
        saveComponentDraft(componentId, request, null);
    }

    @Transactional(transactionManager = "tenantTransactionManager")
    public void saveComponentDraft(Long componentId, UpdateComponentCompositeRequest request, Long userId) {
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
        saveComponentOverride(component, componentPayload, userId);

        if (request.translations() == null) {
            activityPublisher.publishComponentEvent(component.getId(), component.getName(), ActivityAction.DRAFT_SAVED, userId, null, null);
            return;
        }

        for (Map.Entry<Language, ComponentI18nUpdateCommand> entry : request.translations().entrySet()) {
            ComponentI18nUpdateCommand command = entry.getValue();
            if (command == null) {
                continue;
            }
            ComponentI18nDraftPayload payload = new ComponentI18nDraftPayload(
                command.hasTitle() ? command.title() : null,
                command.hasTitle(),
                command.hasSubtitle() ? command.subtitle() : null,
                command.hasSubtitle(),
                command.hasDescription() ? command.description() : null,
                command.hasDescription());
            saveComponentI18nOverride(component.getId(), entry.getKey(), payload, userId);
        }
        activityPublisher.publishComponentEvent(component.getId(), component.getName(), ActivityAction.DRAFT_SAVED, userId, null, null);
    }

    @Transactional(transactionManager = "tenantTransactionManager")
    public void saveComponentEntryDraft(Long entryId, UpdateComponentEntryCompositeRequest request, Long userId) {
        ComponentEntry entry = componentEntryRepository.findById(entryId)
            .orElseThrow(() -> new EntityNotFoundException("ComponentEntry", entryId));
        Component component = componentRepository.findById(entry.getComponentId())
            .orElseThrow(() -> new EntityNotFoundException("Component", entry.getComponentId()));

        if (request.responsiveMediaId() != null) {
            responsiveMediaSetRepository.findById(request.responsiveMediaId())
                .orElseThrow(() -> new EntityNotFoundException("ResponsiveMediaSet", request.responsiveMediaId()));
        }

        ComponentEntryDraftPayload entryPayload = new ComponentEntryDraftPayload(
            request.sortOrder(),
            request.isVisible(),
            request.styleClasses(),
            request.responsiveMediaId());
        saveComponentEntryOverride(entry, entryPayload, userId);

        if (request.translations() != null) {
            for (Map.Entry<Language, EntryI18nUpdateCommand> translation : request.translations().entrySet()) {
                EntryI18nUpdateCommand command = translation.getValue();
                if (command == null) {
                    continue;
                }
                ComponentEntryI18nDraftPayload payload = new ComponentEntryI18nDraftPayload(
                    command.hasTitle() ? command.title() : null,
                    command.hasTitle(),
                    command.hasDescription() ? command.description() : null,
                    command.hasDescription(),
                    command.hasDynamicFields() ? command.dynamicFields() : null,
                    command.hasDynamicFields());
                saveComponentEntryI18nOverride(entry.getId(), translation.getKey(), payload, userId);
            }
        }
        activityPublisher.publishComponentEvent(component.getId(), component.getName(), ActivityAction.DRAFT_SAVED, userId, null, null);
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

        Set<Long> pageEntryIds = findPageEntryIds(pageComponentIds);
        List<CmsDraftOverride> entryDrafts = pageEntryIds.isEmpty()
            ? List.of()
            : draftOverrideRepository
                .findByTargetTypeAndTargetIdIn(CmsDraftTargetType.COMPONENT_ENTRY, pageEntryIds)
                .stream()
                .filter(draft -> CmsDraftOverride.NO_LANGUAGE.equals(draft.getLanguageKey()))
                .toList();
        for (CmsDraftOverride draft : entryDrafts) {
            applyComponentEntryDraft(draft);
        }

        List<CmsDraftOverride> entryI18nDrafts = pageEntryIds.isEmpty()
            ? List.of()
            : draftOverrideRepository
                .findByTargetTypeAndTargetIdIn(CmsDraftTargetType.COMPONENT_ENTRY_I18N, pageEntryIds)
                .stream()
                .filter(draft -> language.name().equals(draft.getLanguageKey()))
                .toList();
        for (CmsDraftOverride draft : entryI18nDrafts) {
            applyComponentEntryI18nDraft(draft);
        }

        draftOverrideRepository.deleteAll(componentDrafts);
        draftOverrideRepository.deleteAll(i18nDrafts);
        draftOverrideRepository.deleteAll(entryDrafts);
        draftOverrideRepository.deleteAll(entryI18nDrafts);
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

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public Map<Long, ComponentEntryDraftPayload> findComponentEntryDrafts(Collection<Long> entryIds) {
        if (entryIds == null || entryIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, ComponentEntryDraftPayload> result = new LinkedHashMap<>();
        draftOverrideRepository.findByTargetTypeAndTargetIdIn(CmsDraftTargetType.COMPONENT_ENTRY, entryIds)
            .stream()
            .filter(draft -> CmsDraftOverride.NO_LANGUAGE.equals(draft.getLanguageKey()))
            .forEach(draft -> result.put(draft.getTargetId(), readPayload(draft.getPayload(), ComponentEntryDraftPayload.class)));
        return result;
    }

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public Map<String, ComponentEntryI18nDraftPayload> findComponentEntryI18nDrafts(Collection<Long> entryIds, Language language) {
        if (language == null || entryIds == null || entryIds.isEmpty()) {
            return Map.of();
        }
        Map<String, ComponentEntryI18nDraftPayload> result = new LinkedHashMap<>();
        draftOverrideRepository.findByTargetTypeAndTargetIdIn(CmsDraftTargetType.COMPONENT_ENTRY_I18N, entryIds)
            .stream()
            .filter(draft -> language.name().equals(draft.getLanguageKey()))
            .forEach(draft -> result.put(i18nKey(draft.getTargetId(), language),
                readPayload(draft.getPayload(), ComponentEntryI18nDraftPayload.class)));
        return result;
    }

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public Map<String, ComponentEntryI18nDraftPayload> findComponentEntryI18nDrafts(Collection<Long> entryIds) {
        if (entryIds == null || entryIds.isEmpty()) {
            return Map.of();
        }
        Map<String, ComponentEntryI18nDraftPayload> result = new LinkedHashMap<>();
        draftOverrideRepository.findByTargetTypeAndTargetIdIn(CmsDraftTargetType.COMPONENT_ENTRY_I18N, entryIds)
            .forEach(draft -> result.put(draft.getTargetId() + ":" + draft.getLanguageKey(),
                readPayload(draft.getPayload(), ComponentEntryI18nDraftPayload.class)));
        return result;
    }

    public String i18nKey(Long componentId, Language language) {
        return componentId + ":" + language.name();
    }

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public SmartEditDraftOverviewResponse listPageDrafts(Long pageId, Language language) {
        return listPageDrafts(pageId, language, Locale.getDefault());
    }

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public SmartEditDraftOverviewResponse listPageDrafts(Long pageId, Language language, Locale locale) {
        return SmartEditDraftOverviewResponse.of(buildPageDraftItems(pageId, language, locale));
    }

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public SmartEditDraftOverviewResponse buildPublishReview(Long pageId, Language language) {
        return buildPublishReview(pageId, language, Locale.getDefault());
    }

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public SmartEditDraftOverviewResponse buildPublishReview(Long pageId, Language language, Locale locale) {
        return SmartEditDraftOverviewResponse.of(buildPageDraftItems(pageId, language, locale).stream()
            .filter(item -> item.fieldChanges() != null && !item.fieldChanges().isEmpty())
            .toList());
    }

    @Transactional(transactionManager = "tenantTransactionManager")
    public void discardDraft(Long draftId, Long userId) {
        CmsDraftOverride draft = draftOverrideRepository.findById(draftId)
            .orElseThrow(() -> new EntityNotFoundException("CmsDraftOverride", draftId));
        publishDiscardActivity(draft, userId);
        draftOverrideRepository.delete(draft);
    }

    @Transactional(transactionManager = "tenantTransactionManager")
    public int discardPageDrafts(Long pageId, Language language, Long userId) {
        Set<Long> componentIds = findPageComponentIds(pageId);
        Set<Long> entryIds = findPageEntryIds(componentIds);
        List<CmsDraftOverride> drafts = findPageScopedDraftRows(componentIds, entryIds, language);
        drafts.forEach(draft -> publishDiscardActivity(draft, userId));
        draftOverrideRepository.deleteAll(drafts);
        return drafts.size();
    }

    @Transactional(transactionManager = "tenantTransactionManager")
    public int discardDraftGroup(Long pageId, Language language, String groupKey, Long userId) {
        if (groupKey == null || groupKey.isBlank()) {
            return 0;
        }
        List<SmartEditDraftItemResponse> matchingItems = buildPageDraftItems(pageId, language, Locale.getDefault())
            .stream()
            .filter(item -> groupKey.equals(SmartEditDraftGrouping.groupKey(item)))
            .toList();
        if (matchingItems.isEmpty()) {
            return 0;
        }
        Set<Long> draftIds = matchingItems.stream()
            .map(SmartEditDraftItemResponse::draftId)
            .collect(Collectors.toSet());
        Set<Long> componentIds = findPageComponentIds(pageId);
        Set<Long> entryIds = findPageEntryIds(componentIds);
        List<CmsDraftOverride> drafts = findPageScopedDraftRows(componentIds, entryIds, language)
            .stream()
            .filter(draft -> draftIds.contains(draft.getId()))
            .toList();
        drafts.forEach(draft -> publishDiscardActivity(draft, userId));
        draftOverrideRepository.deleteAll(drafts);
        return drafts.size();
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
        if (titlePresent(draft)) {
            i18n.setTitle(draft.title());
        }
        if (subtitlePresent(draft)) {
            i18n.setSubtitle(draft.subtitle());
        }
        if (descriptionPresent(draft)) {
            i18n.setDescription(draft.description());
        }
        return i18n;
    }

    public ComponentEntry apply(ComponentEntry entry, ComponentEntryDraftPayload draft) {
        if (draft == null) {
            return entry;
        }
        if (draft.sortOrder() != null) {
            entry.setSortOrder(draft.sortOrder());
        }
        if (draft.isVisible() != null) {
            entry.setIsVisible(draft.isVisible());
        }
        if (draft.styleClasses() != null) {
            entry.setStyleClasses(draft.styleClasses());
        }
        if (draft.responsiveMediaId() != null) {
            entry.setResponsiveMedia(responsiveMediaSetRepository.findById(draft.responsiveMediaId())
                .orElseThrow(() -> new EntityNotFoundException("ResponsiveMediaSet", draft.responsiveMediaId())));
        }
        return entry;
    }

    public ComponentEntryI18n apply(ComponentEntryI18n i18n, ComponentEntryI18nDraftPayload draft) {
        if (draft == null) {
            return i18n;
        }
        if (titlePresent(draft)) {
            i18n.setTitle(draft.title());
        }
        if (descriptionPresent(draft)) {
            i18n.setDescription(draft.description());
        }
        if (dynamicFieldsPresent(draft)) {
            i18n.setCustomData(draft.dynamicFields() == null ? null : writePayload(draft.dynamicFields()));
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

    private void applyComponentEntryDraft(CmsDraftOverride draft) {
        componentEntryRepository.findById(draft.getTargetId()).ifPresent(entry -> {
            apply(entry, readPayload(draft.getPayload(), ComponentEntryDraftPayload.class));
            ComponentEntry saved = componentEntryRepository.save(entry);
            componentMediaLinkSyncService.syncEntryResponsiveLinks(saved);
        });
    }

    private void applyComponentEntryI18nDraft(CmsDraftOverride draft) {
        Language language = Language.valueOf(draft.getLanguageKey());
        ComponentEntryI18n i18n = componentEntryI18nRepository
            .findByEntryIdAndLanguage(draft.getTargetId(), language)
            .orElseGet(() -> {
                ComponentEntryI18n created = new ComponentEntryI18n();
                created.setEntryId(draft.getTargetId());
                created.setLanguage(language);
                created.setStatus(ComponentStatus.PUBLISHED);
                return created;
            });
        apply(i18n, readPayload(draft.getPayload(), ComponentEntryI18nDraftPayload.class));
        i18n.setStatus(ComponentStatus.PUBLISHED);
        i18n.publish();
        componentEntryI18nRepository.save(i18n);
    }

    private List<SmartEditDraftItemResponse> buildPageDraftItems(Long pageId, Language language, Locale locale) {
        Set<Long> componentIds = findPageComponentIds(pageId);
        Set<Long> entryIds = findPageEntryIds(componentIds);
        List<CmsDraftOverride> drafts = findPageScopedDraftRows(componentIds, entryIds, language);
        Map<Long, Component> componentMap = componentIds.isEmpty()
            ? Map.of()
            : componentRepository.findByIdIn(componentIds.stream().toList()).stream().collect(Collectors.toMap(Component::getId, component -> component));
        Map<Long, ComponentEntry> entryMap = entryIds.isEmpty()
            ? Map.of()
            : componentEntryRepository.findByIdIn(entryIds.stream().toList()).stream()
                .collect(Collectors.toMap(ComponentEntry::getId, entry -> entry));
        Map<Long, ComponentI18n> componentI18nMap = componentIds.isEmpty() || language == null
            ? Map.of()
            : componentI18nRepository.findByComponentIdInAndLanguage(componentIds.stream().toList(), language).stream()
                .collect(Collectors.toMap(ComponentI18n::getComponentId, i18n -> i18n));
        Map<Long, ComponentEntryI18n> entryI18nMap = entryIds.isEmpty() || language == null
            ? Map.of()
            : componentEntryI18nRepository.findByEntryIdInAndLanguage(entryIds.stream().toList(), language).stream()
                .collect(Collectors.toMap(ComponentEntryI18n::getEntryId, i18n -> i18n));

        return drafts.stream()
            .map(draft -> toDraftItem(draft, componentMap, entryMap, componentI18nMap, entryI18nMap, locale))
            .filter(item -> item.fieldChanges() != null && !item.fieldChanges().isEmpty())
            .sorted(Comparator.comparing(SmartEditDraftItemResponse::updatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    private SmartEditDraftItemResponse toDraftItem(
        CmsDraftOverride draft,
        Map<Long, Component> componentMap,
        Map<Long, ComponentEntry> entryMap,
        Map<Long, ComponentI18n> componentI18nMap,
        Map<Long, ComponentEntryI18n> entryI18nMap,
        Locale locale) {

        Component component = null;
        ComponentEntry entry = null;
        List<SmartEditDraftFieldChange> changes = List.of();

        if (draft.getTargetType() == CmsDraftTargetType.COMPONENT) {
            component = componentMap.get(draft.getTargetId());
            changes = displayChanges(componentChanges(component, readPayload(draft.getPayload(), ComponentDraftPayload.class), locale), locale);
        } else if (draft.getTargetType() == CmsDraftTargetType.COMPONENT_I18N) {
            component = componentMap.get(draft.getTargetId());
            changes = displayChanges(componentI18nChanges(componentI18nMap.get(draft.getTargetId()),
                readPayload(draft.getPayload(), ComponentI18nDraftPayload.class), locale), locale);
        } else if (draft.getTargetType() == CmsDraftTargetType.COMPONENT_ENTRY) {
            entry = entryMap.get(draft.getTargetId());
            component = entry != null ? componentMap.get(entry.getComponentId()) : null;
            changes = displayChanges(entryChanges(entry, readPayload(draft.getPayload(), ComponentEntryDraftPayload.class), locale), locale);
        } else if (draft.getTargetType() == CmsDraftTargetType.COMPONENT_ENTRY_I18N) {
            entry = entryMap.get(draft.getTargetId());
            component = entry != null ? componentMap.get(entry.getComponentId()) : null;
            changes = displayChanges(entryI18nChanges(entryI18nMap.get(draft.getTargetId()),
                readPayload(draft.getPayload(), ComponentEntryI18nDraftPayload.class), locale), locale);
        }

        return new SmartEditDraftItemResponse(
            draft.getId(),
            draft.getTargetType(),
            draft.getTargetId(),
            CmsDraftOverride.NO_LANGUAGE.equals(draft.getLanguageKey()) ? null : draft.getLanguageKey(),
            component != null ? component.getId() : null,
            component != null ? component.getUid() : null,
            component != null ? component.getName() : null,
            entry != null ? entry.getId() : null,
            entry != null ? entry.getUid() : null,
            changes,
            draft.getUpdatedAt(),
            draft.getUpdatedBy());
    }

    private List<CmsDraftOverride> findPageScopedDraftRows(Set<Long> componentIds, Set<Long> entryIds, Language language) {
        if (componentIds.isEmpty() && entryIds.isEmpty()) {
            return List.of();
        }
        List<CmsDraftOverride> result = new java.util.ArrayList<>();
        if (!componentIds.isEmpty()) {
            result.addAll(draftOverrideRepository.findByTargetTypeAndTargetIdIn(CmsDraftTargetType.COMPONENT, componentIds)
                .stream()
                .filter(draft -> CmsDraftOverride.NO_LANGUAGE.equals(draft.getLanguageKey()))
                .toList());
            if (language != null) {
                result.addAll(draftOverrideRepository.findByTargetTypeAndTargetIdIn(CmsDraftTargetType.COMPONENT_I18N, componentIds)
                    .stream()
                    .filter(draft -> language.name().equals(draft.getLanguageKey()))
                    .toList());
            }
        }
        if (!entryIds.isEmpty()) {
            result.addAll(draftOverrideRepository.findByTargetTypeAndTargetIdIn(CmsDraftTargetType.COMPONENT_ENTRY, entryIds)
                .stream()
                .filter(draft -> CmsDraftOverride.NO_LANGUAGE.equals(draft.getLanguageKey()))
                .toList());
            if (language != null) {
                result.addAll(draftOverrideRepository.findByTargetTypeAndTargetIdIn(CmsDraftTargetType.COMPONENT_ENTRY_I18N, entryIds)
                    .stream()
                    .filter(draft -> language.name().equals(draft.getLanguageKey()))
                    .toList());
            }
        }
        return result;
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

    private Set<Long> findPageEntryIds(Set<Long> componentIds) {
        if (componentIds == null || componentIds.isEmpty()) {
            return Set.of();
        }
        return componentEntryRepository.findByComponentIdInAndStatusInOrderBySortOrder(
                componentIds.stream().toList(), CmsVisibility.componentStatuses(true))
            .stream()
            .map(ComponentEntry::getId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
    }

    private List<SmartEditDraftFieldChange> componentChanges(Component component, ComponentDraftPayload draft, Locale locale) {
        if (draft == null) return List.of();
        List<SmartEditDraftFieldChange> changes = new java.util.ArrayList<>();
        addChange(changes, "name", label("cms.preview.field.name", locale), component != null ? component.getName() : null, draft.name(), "text");
        addChange(changes, "displayOrder", label("cms.preview.field.displayOrder", locale), component != null ? component.getDisplayOrder() : null, draft.displayOrder(), "number");
        addChange(changes, "isVisible", label("cms.preview.field.isVisible", locale), component != null ? component.getIsVisible() : null, draft.isVisible(), "boolean");
        addChange(changes, "styleClasses", label("cms.preview.field.styleClasses", locale), component != null ? component.getStyleClasses() : null, draft.styleClasses(), "text");
        addChange(changes, "responsiveMediaId", label("cms.preview.field.responsiveMedia", locale), responsiveMediaValue(component != null && component.getResponsiveMedia() != null ? component.getResponsiveMedia().getId() : null), responsiveMediaValue(draft.responsiveMediaId()), "media");
        addChange(changes, "navigationNodeId", label("cms.preview.field.navigationNode", locale), navigationNodeValue(component != null ? component.getNavigationNodeId() : null), navigationNodeValue(draft.navigationNodeId()), "link");
        addChange(changes, "navigationType", label("cms.preview.field.navigationType", locale), component != null ? component.getNavigationType() : null, draft.navigationType(), "text");
        addChange(changes, "searchBox", label("cms.preview.field.searchBox", locale), component != null ? component.getSearchBox() : null, draft.searchBox(), "boolean");
        return changes;
    }

    private void saveComponentOverride(Component component, ComponentDraftPayload payload, Long userId) {
        if (componentChanges(component, payload, Locale.getDefault()).isEmpty()) {
            return;
        }
        saveOverride(CmsDraftTargetType.COMPONENT, component.getId(), CmsDraftOverride.NO_LANGUAGE, payload, userId);
    }

    private void saveComponentI18nOverride(Long componentId, Language language, ComponentI18nDraftPayload payload, Long userId) {
        ComponentI18n i18n = componentI18nRepository.findByComponentIdAndLanguage(componentId, language).orElse(null);
        if (componentI18nChanges(i18n, payload, Locale.getDefault()).isEmpty()) {
            return;
        }
        saveOverride(CmsDraftTargetType.COMPONENT_I18N, componentId, language.name(), payload, userId);
    }

    private void saveComponentEntryOverride(ComponentEntry entry, ComponentEntryDraftPayload payload, Long userId) {
        if (entryChanges(entry, payload, Locale.getDefault()).isEmpty()) {
            return;
        }
        saveOverride(CmsDraftTargetType.COMPONENT_ENTRY, entry.getId(), CmsDraftOverride.NO_LANGUAGE, payload, userId);
    }

    private void saveComponentEntryI18nOverride(Long entryId, Language language, ComponentEntryI18nDraftPayload payload, Long userId) {
        ComponentEntryI18n i18n = componentEntryI18nRepository.findByEntryIdAndLanguage(entryId, language).orElse(null);
        if (entryI18nChanges(i18n, payload, Locale.getDefault()).isEmpty()) {
            return;
        }
        saveOverride(CmsDraftTargetType.COMPONENT_ENTRY_I18N, entryId, language.name(), payload, userId);
    }

    private List<SmartEditDraftFieldChange> componentI18nChanges(ComponentI18n i18n, ComponentI18nDraftPayload draft, Locale locale) {
        if (draft == null) return List.of();
        List<SmartEditDraftFieldChange> changes = new java.util.ArrayList<>();
        addChange(changes, "title", label("cms.preview.field.title", locale), i18n != null ? i18n.getTitle() : null, draft.title(), "text", titlePresent(draft));
        addChange(changes, "subtitle", label("cms.preview.field.subtitle", locale), i18n != null ? i18n.getSubtitle() : null, draft.subtitle(), "text", subtitlePresent(draft));
        addChange(changes, "description", label("cms.preview.field.description", locale), i18n != null ? i18n.getDescription() : null, draft.description(), "text", descriptionPresent(draft));
        return changes;
    }

    private List<SmartEditDraftFieldChange> entryChanges(ComponentEntry entry, ComponentEntryDraftPayload draft, Locale locale) {
        if (draft == null) return List.of();
        List<SmartEditDraftFieldChange> changes = new java.util.ArrayList<>();
        addChange(changes, "sortOrder", label("cms.preview.field.sortOrder", locale), entry != null ? entry.getSortOrder() : null, draft.sortOrder(), "number");
        addChange(changes, "isVisible", label("cms.preview.field.isVisible", locale), entry != null ? entry.getIsVisible() : null, draft.isVisible(), "boolean");
        addChange(changes, "styleClasses", label("cms.preview.field.styleClasses", locale), entry != null ? entry.getStyleClasses() : null, draft.styleClasses(), "text");
        addChange(changes, "responsiveMediaId", label("cms.preview.field.responsiveMedia", locale), responsiveMediaValue(entry != null && entry.getResponsiveMedia() != null ? entry.getResponsiveMedia().getId() : null), responsiveMediaValue(draft.responsiveMediaId()), "media");
        return changes;
    }

    private List<SmartEditDraftFieldChange> entryI18nChanges(ComponentEntryI18n i18n, ComponentEntryI18nDraftPayload draft, Locale locale) {
        if (draft == null) return List.of();
        List<SmartEditDraftFieldChange> changes = new java.util.ArrayList<>();
        addChange(changes, "title", label("cms.preview.field.title", locale), i18n != null ? i18n.getTitle() : null, draft.title(), "text", titlePresent(draft));
        addChange(changes, "description", label("cms.preview.field.description", locale), i18n != null ? i18n.getDescription() : null, draft.description(), "text", descriptionPresent(draft));
        Map<String, Object> beforeFields = parseMap(i18n != null ? i18n.getCustomData() : null);
        if (dynamicFieldsPresent(draft)) {
            Map<String, Object> afterFields = Optional.ofNullable(draft.dynamicFields()).orElseGet(Map::of);
            Set<String> fieldKeys = new java.util.LinkedHashSet<>();
            fieldKeys.addAll(beforeFields.keySet());
            fieldKeys.addAll(afterFields.keySet());
            fieldKeys.forEach(key -> addChange(changes, "dynamicFields." + key, key, beforeFields.get(key), afterFields.get(key), "text", true));
        }
        return changes;
    }

    private List<SmartEditDraftFieldChange> displayChanges(List<SmartEditDraftFieldChange> changes, Locale locale) {
        return changes.stream()
            .map(change -> displayChange(change, locale))
            .toList();
    }

    private SmartEditDraftFieldChange displayChange(SmartEditDraftFieldChange change, Locale locale) {
        boolean isMedia = "media".equals(change.valueType());
        return new SmartEditDraftFieldChange(
            change.field(),
            change.label(),
            change.before(),
            change.after(),
            change.valueType(),
            formatDraftValue(change.before(), locale),
            formatDraftValue(change.after(), locale),
            mediaPreviews(change.before(), locale),
            mediaPreviews(change.after(), locale),
            isMedia);
    }

    private String formatDraftValue(Object value, Locale locale) {
        if (value == null || (value instanceof String text && text.isBlank())) {
            return label("cms.preview.value.empty", locale);
        }
        if (value instanceof Boolean bool) {
            return bool ? label("cms.preview.value.yes", locale) : label("cms.preview.value.no", locale);
        }
        if (value instanceof Number || value instanceof String || value instanceof Enum<?>) {
            return String.valueOf(value);
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                .map(item -> formatDraftValue(item, locale))
                .collect(Collectors.joining(", "));
        }
        if (value instanceof Map<?, ?> map) {
            Object label = firstPresent(map, "label", "originalName", "fileName", "code", "uid", "id");
            if (label != null) {
                return String.valueOf(label);
            }
        }
        return String.valueOf(value);
    }

    private List<SmartEditMediaPreviewResponse> mediaPreviews(Object value, Locale locale) {
        if (!(value instanceof Map<?, ?> map)) {
            return List.of(new SmartEditMediaPreviewResponse(formatDraftValue(value, locale), null));
        }
        List<SmartEditMediaPreviewResponse> responsiveItems = new java.util.ArrayList<>();
        addResponsiveMediaPreview(responsiveItems, map, "desktopMedia", label("cms.preview.value.desktop", locale), locale);
        addResponsiveMediaPreview(responsiveItems, map, "mobileMedia", label("cms.preview.value.mobile", locale), locale);
        if (!responsiveItems.isEmpty()) {
            return responsiveItems;
        }
        return List.of(new SmartEditMediaPreviewResponse(formatDraftValue(value, locale), mediaUrl(map)));
    }

    private void addResponsiveMediaPreview(
        List<SmartEditMediaPreviewResponse> items,
        Map<?, ?> map,
        String key,
        String slotLabel,
        Locale locale) {
        Object value = map.get(key);
        if (value != null) {
            items.add(new SmartEditMediaPreviewResponse(slotLabel + ": " + formatDraftValue(value, locale), mediaUrl(value)));
        }
    }

    private String mediaUrl(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return null;
        }
        Object url = firstPresent(map, "thumbnailUrl", "previewUrl", "url", "publicUrl", "src");
        return url instanceof String text && !text.isBlank() ? text : null;
    }

    private Object firstPresent(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private void addChange(List<SmartEditDraftFieldChange> changes, String field, String label, Object before, Object after, String valueType) {
        addChange(changes, field, label, before, after, valueType, after != null);
    }

    private void addChange(List<SmartEditDraftFieldChange> changes, String field, String label, Object before, Object after, String valueType, boolean present) {
        if (!present || equivalentDraftValue(before, after)) {
            return;
        }
        changes.add(new SmartEditDraftFieldChange(field, label, before, after, valueType));
    }

    private boolean equivalentDraftValue(Object before, Object after) {
        if (before instanceof String || after instanceof String) {
            String normalizedBefore = normalizeDraftText(before);
            String normalizedAfter = normalizeDraftText(after);
            return java.util.Objects.equals(normalizedBefore, normalizedAfter);
        }
        return java.util.Objects.equals(before, after);
    }

    private String normalizeDraftText(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString().trim();
    }

    private String label(String key, Locale locale) {
        return messageSource.getMessage(key, null, locale);
    }

    private boolean titlePresent(ComponentI18nDraftPayload draft) {
        return draft.titlePresent() || draft.title() != null;
    }

    private boolean subtitlePresent(ComponentI18nDraftPayload draft) {
        return draft.subtitlePresent() || draft.subtitle() != null;
    }

    private boolean descriptionPresent(ComponentI18nDraftPayload draft) {
        return draft.descriptionPresent() || draft.description() != null;
    }

    private boolean titlePresent(ComponentEntryI18nDraftPayload draft) {
        return draft.titlePresent() || draft.title() != null;
    }

    private boolean descriptionPresent(ComponentEntryI18nDraftPayload draft) {
        return draft.descriptionPresent() || draft.description() != null;
    }

    private boolean dynamicFieldsPresent(ComponentEntryI18nDraftPayload draft) {
        return draft.dynamicFieldsPresent() || draft.dynamicFields() != null;
    }

    private Object responsiveMediaValue(Long mediaSetId) {
        if (mediaSetId == null) {
            return null;
        }
        return responsiveMediaSetRepository.findById(mediaSetId)
            .<Object>map(this::responsiveMediaValue)
            .orElse(mediaSetId);
    }

    private Map<String, Object> responsiveMediaValue(ResponsiveMediaSet mediaSet) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", mediaSet.getId());
        if (mediaSet.getUid() != null) {
            value.put("uid", mediaSet.getUid());
        }
        if (mediaSet.getCode() != null) {
            value.put("code", mediaSet.getCode());
            value.put("label", mediaSet.getCode());
        }
        if (mediaSet.getDesktopMedia() != null) {
            value.put("desktopMedia", mediaLabel(mediaSet.getDesktopMedia()));
        }
        if (mediaSet.getMobileMedia() != null) {
            value.put("mobileMedia", mediaLabel(mediaSet.getMobileMedia()));
        }
        return value;
    }

    private Map<String, Object> mediaLabel(com.backend.domain.entity.Media media) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", media.getId());
        if (media.getUid() != null) {
            value.put("uid", media.getUid());
        }
        if (media.getOriginalName() != null) {
            value.put("label", media.getOriginalName());
        } else if (media.getFileName() != null) {
            value.put("label", media.getFileName());
        }
        value.put("publicUrl", media.getPublicUrl());
        return value;
    }

    private Object navigationNodeValue(Long nodeId) {
        if (nodeId == null) {
            return null;
        }
        return navigationNodeRepository.findById(nodeId)
            .<Object>map(this::navigationNodeValue)
            .orElse(nodeId);
    }

    private Map<String, Object> navigationNodeValue(NavigationNode node) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", node.getId());
        if (node.getUid() != null) {
            value.put("uid", node.getUid());
            value.put("label", node.getUid());
        }
        return value;
    }

    private Map<String, Object> parseMap(String payload) {
        if (payload == null || payload.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(payload, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            String correlationId = MDC.get("correlationId");
            log.error("Failed to parse CMS draft custom data payload correlationId={} payloadLength={}",
                correlationId, payload.length(), ex);
            throw new IllegalStateException("Failed to parse CMS draft custom data payload", ex);
        }
    }

    private void publishDiscardActivity(CmsDraftOverride draft, Long userId) {
        Long componentId = draft.getTargetId();
        String componentName = "draft";
        if (draft.getTargetType() == CmsDraftTargetType.COMPONENT_ENTRY || draft.getTargetType() == CmsDraftTargetType.COMPONENT_ENTRY_I18N) {
            Optional<ComponentEntry> entry = componentEntryRepository.findById(draft.getTargetId());
            componentId = entry.map(ComponentEntry::getComponentId).orElse(null);
        }
        if (componentId == null) {
            log.warn("Skipping draft discard activity because component could not be resolved for draft {}", draft.getId());
            return;
        }
        Optional<Component> component = componentRepository.findById(componentId);
        if (component.isPresent()) {
            componentName = component.get().getName();
        }
        activityPublisher.publishComponentEvent(componentId, componentName, ActivityAction.DRAFT_DISCARDED, userId, null, null);
    }

    private void saveOverride(CmsDraftTargetType targetType, Long targetId, String languageKey, Object payload) {
        saveOverride(targetType, targetId, languageKey, payload, null);
    }

    private void saveOverride(CmsDraftTargetType targetType, Long targetId, String languageKey, Object payload, Long userId) {
        CmsDraftOverride draft = draftOverrideRepository
            .findByTargetTypeAndTargetIdAndLanguageKey(targetType, targetId, languageKey)
            .orElseGet(CmsDraftOverride::new);
        if (draft.getId() == null) {
            draft.setCreatedBy(userId);
        }
        draft.setTargetType(targetType);
        draft.setTargetId(targetId);
        draft.setLanguageKey(languageKey);
        draft.setPayload(writePayload(payload));
        draft.setUpdatedBy(userId);
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
