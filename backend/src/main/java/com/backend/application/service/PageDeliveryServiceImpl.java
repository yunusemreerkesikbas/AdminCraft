package com.backend.application.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.cms.preview.CmsRequestContext;
import com.backend.application.cms.preview.CmsDraftOverrideService;
import com.backend.application.cms.preview.CmsVisibility;
import com.backend.application.dto.delivery.ComponentDeliveryResponse;
import com.backend.application.util.UrlUtils;
import com.backend.application.dto.delivery.ContentSlotDeliveryResponse;
import com.backend.application.dto.delivery.ContentSlotsWrapper;
import com.backend.application.dto.delivery.EntryDeliveryResponse;
import com.backend.application.dto.delivery.NavigationDeliveryResponse;
import com.backend.application.dto.delivery.PageDeliveryResponse;
import com.backend.application.dto.delivery.ResponsiveMediaDeliveryResponse;
import com.backend.application.util.MediaFieldExpander;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.entity.ComponentType;
import com.backend.domain.entity.Page;
import com.backend.domain.entity.PageI18n;
import com.backend.domain.entity.PageSlot;
import com.backend.domain.entity.PageTemplate;
import com.backend.domain.entity.ResponsiveMediaSet;
import com.backend.domain.entity.SlotComponent;
import com.backend.domain.entity.TemplateSlot;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.NavigationType;
import com.backend.domain.enums.PageStatus;
import com.backend.domain.enums.PageType;
import com.backend.domain.enums.RobotTag;
import com.backend.domain.repository.ComponentEntryI18nRepository;
import com.backend.domain.repository.ComponentEntryRepository;
import com.backend.domain.repository.ComponentI18nRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.domain.repository.PageI18nRepository;
import com.backend.domain.repository.PageRepository;
import com.backend.domain.repository.PageSlotRepository;
import com.backend.domain.repository.PageTemplateRepository;
import com.backend.domain.repository.ResponsiveMediaSetRepository;
import com.backend.domain.repository.SlotComponentRepository;
import com.backend.domain.repository.TemplateSlotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
public class PageDeliveryServiceImpl implements PageDeliveryService {

        private static final Set<String> RESERVED_FIELDS = Set.of(
                        "uid", "order", "title", "description", "isVisible", "styleClasses", "responsive", "isExternal");

        private final PageRepository pageRepository;
        private final PageI18nRepository pageI18nRepository;
        private final PageSlotRepository pageSlotRepository;
        private final PageTemplateRepository pageTemplateRepository;
        private final TemplateSlotRepository templateSlotRepository;
        private final SlotComponentRepository slotComponentRepository;
        private final ComponentRepository componentRepository;
        private final ComponentTypeRepository componentTypeRepository;
        private final ComponentI18nRepository componentI18nRepository;
        private final ComponentEntryRepository componentEntryRepository;
        private final ComponentEntryI18nRepository componentEntryI18nRepository;
        private final ResponsiveMediaSetRepository responsiveMediaSetRepository;
        private final ResponsiveMediaService responsiveMediaService;
        private final NavigationService navigationService;
        private final MediaFieldExpander mediaFieldExpander;
        private final CmsRequestContext cmsRequestContext;
        private final CmsDraftOverrideService cmsDraftOverrideService;

        @Override
        public Optional<PageDeliveryResponse> resolvePageForDelivery(String pageType, String pageLabelOrId,
                        String code, Long previewPageId, Language lang) {
                Optional<Page> pageOpt;
                String normalizedPageLabelOrId = normalizePageLabelOrId(pageLabelOrId);
                String normalizedCode = normalizeParam(code);
                final String[] codeToInclude = { null };

                if (cmsRequestContext.isPreview()) {
                        Long boundPageId = cmsRequestContext.getPreviewPageId();
                        if (boundPageId != null && previewPageId == null) {
                                return Optional.empty();
                        }
                }

                if (cmsRequestContext.isPreview() && previewPageId != null) {
                        pageOpt = resolvePreviewPageById(previewPageId);
                        return pageOpt
                                        .filter(page -> isPageVisibleForCurrentMode(page, lang))
                                        .map(page -> buildPageDeliveryResponse(page, lang,
                                                        previewDeliveryCode(page, normalizedCode)));
                }

                if (pageType == null || pageType.isBlank()) {
                        pageOpt = resolveHomepage();
                } else {
                        Optional<PageType> resolvedType = PageType.fromTypeCode(pageType);
                        if (resolvedType.isEmpty()) {
                                pageOpt = Optional.empty();
                        } else {
                                switch (resolvedType.get()) {
                                        case CONTENT -> pageOpt = normalizedPageLabelOrId != null
                                                        ? resolvePageByCanonicalUrl(lang, normalizedPageLabelOrId, PageType.CONTENT)
                                                        : Optional.empty();
                                        case PRODUCT -> {
                                                if (normalizedCode != null) {
                                                        pageOpt = resolveUniqueTemplatePage(PageType.PRODUCT);
                                                        codeToInclude[0] = normalizedCode;
                                                } else {
                                                        pageOpt = Optional.empty();
                                                }
                                        }
                                        case CATEGORY -> {
                                                if (normalizedCode != null) {
                                                        pageOpt = resolveUniqueTemplatePage(PageType.CATEGORY);
                                                        codeToInclude[0] = normalizedCode;
                                                } else {
                                                        pageOpt = Optional.empty();
                                                }
                                        }
                                        case SEARCH -> pageOpt = resolveUniqueTemplatePage(PageType.SEARCH);
                                        case LANDING -> pageOpt = normalizedPageLabelOrId != null
                                                        ? resolvePageByCanonicalUrl(lang, normalizedPageLabelOrId, PageType.LANDING)
                                                        : Optional.empty();
                                        default -> pageOpt = Optional.empty();
                                }
                        }
                }

                return pageOpt
                                .filter(page -> isPageVisibleForCurrentMode(page, lang))
                                .map(page -> buildPageDeliveryResponse(page, lang, codeToInclude[0]));
        }

        private Optional<Page> resolvePreviewPageById(Long previewPageId) {
                Long ticketPageId = cmsRequestContext.getPreviewPageId();
                if (ticketPageId != null && !ticketPageId.equals(previewPageId)) {
                        log.debug("Preview page id mismatch: ticketPageId={}, requestedPageId={}", ticketPageId, previewPageId);
                        return Optional.empty();
                }
                return pageRepository.findByIdAndStatusIn(previewPageId, visiblePageStatuses());
        }

        private String previewDeliveryCode(Page page, String normalizedCode) {
                if (normalizedCode == null) {
                        return null;
                }
                PageType pageType = page.getPageType();
                if (pageType == PageType.PRODUCT || pageType == PageType.CATEGORY) {
                        return normalizedCode;
                }
                return null;
        }

        private boolean isPageVisibleForCurrentMode(Page page, Language lang) {
                Set<PageStatus> statuses = visiblePageStatuses();
                return pageI18nRepository.findByPageIdAndLanguage(page.getId(), lang)
                                .filter(i18n -> statuses.contains(i18n.getStatus()))
                                .isPresent();
        }

        private Optional<Page> resolveHomepage() {
                return pageRepository.findByIsHomeTrueAndStatusIn(visiblePageStatuses());
        }

        private Optional<Page> resolveUniqueTemplatePage(PageType pageType) {
                return pageRepository.findFirstByPageTypeAndStatusInOrderByIdAsc(pageType, visiblePageStatuses());
        }

        private Optional<Page> resolvePageByCanonicalUrl(Language lang, String canonicalUrl, PageType pageType) {
                Set<PageStatus> statuses = visiblePageStatuses();
                return pageI18nRepository.findByLanguageAndCanonicalUrlAndStatusIn(lang, canonicalUrl, statuses)
                                .flatMap(i18n -> pageRepository.findByIdAndStatusIn(i18n.getPageId(), statuses))
                                .filter(page -> pageType == page.getPageType());
        }

        private Set<PageStatus> visiblePageStatuses() {
                return CmsVisibility.pageStatuses(cmsRequestContext.isPreview());
        }

        private String normalizeParam(String value) {
                if (value == null) {
                        return null;
                }
                String trimmed = value.trim();
                return trimmed.isEmpty() ? null : trimmed;
        }

        private String normalizePageLabelOrId(String value) {
                String normalized = normalizeParam(value);
                if (normalized == null) {
                        return null;
                }
                if (normalized.startsWith("/")) {
                        return normalized;
                }
                return "/" + normalized;
        }

        private String resolveTypeCode(PageType pageType) {
                if (pageType == null) return "ContentPage";
                return pageType.getTypeCode();
        }

        private PageDeliveryResponse buildPageDeliveryResponse(Page page, Language lang, String code) {
                Optional<PageI18n> i18nOpt = pageI18nRepository.findByPageIdAndLanguage(page.getId(), lang);

                // Fetch template info if assigned
                String templateUid = null;
                if (page.getTemplateId() != null) {
                        Optional<PageTemplate> templateOpt = pageTemplateRepository.findById(page.getTemplateId());
                        templateUid = templateOpt.map(PageTemplate::getUid).orElse(null);
                }

                List<PageSlot> pageSlots = pageSlotRepository.findByPageId(page.getId());
                List<PageSlot> sharedSlots = pageSlotRepository.findSharedSlots();
                List<PageSlot> effectiveSlots = resolveEffectiveSlotsForDelivery(page, pageSlots, sharedSlots);
                List<PageSlot> activeSlots = effectiveSlots.stream()
                                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                                .toList();

                List<Long> slotIds = activeSlots.stream()
                                .map(PageSlot::getId)
                                .filter(id -> id != null)
                                .toList();

                List<SlotComponent> allSlotComponents = slotIds.isEmpty()
                                ? List.of()
                                : slotComponentRepository.findBySlotIdIn(slotIds);
                Map<Long, List<SlotComponent>> componentsBySlotId = allSlotComponents.stream()
                                .collect(Collectors.groupingBy(SlotComponent::getSlotId));

                List<Long> allComponentIds = allSlotComponents.stream()
                                .map(SlotComponent::getComponentId)
                                .distinct()
                                .toList();

                Set<ComponentStatus> visibleComponentStatuses = CmsVisibility.componentStatuses(cmsRequestContext.isPreview());
                Map<Long, Component> componentMap = allComponentIds.isEmpty()
                                ? Map.of()
                                : componentRepository.findByIdIn(allComponentIds).stream()
                                                .filter(c -> visibleComponentStatuses.contains(c.getStatus()))
                                                .map(this::cloneComponent)
                                                .collect(Collectors.toMap(Component::getId, c -> c));

                if (cmsRequestContext.isPreview() && !componentMap.isEmpty()) {
                        Map<Long, com.backend.application.cms.preview.ComponentDraftPayload> drafts =
                                        cmsDraftOverrideService.findComponentDrafts(componentMap.keySet());
                        drafts.forEach((componentId, draft) -> {
                                Component component = componentMap.get(componentId);
                                if (component != null) {
                                        cmsDraftOverrideService.apply(component, draft);
                                }
                        });
                }

                List<Long> publishedComponentIds = componentMap.keySet().stream().toList();

                Map<Long, ComponentI18n> componentI18nMap = publishedComponentIds.isEmpty()
                                ? new LinkedHashMap<>()
                                : new LinkedHashMap<>(
                                                componentI18nRepository.findByComponentIdInAndLanguage(publishedComponentIds, lang)
                                                                .stream()
                                                                .map(this::cloneComponentI18n)
                                                                .collect(Collectors.toMap(ComponentI18n::getComponentId, i -> i)));

                if (cmsRequestContext.isPreview() && !publishedComponentIds.isEmpty()) {
                        Map<String, com.backend.application.cms.preview.ComponentI18nDraftPayload> i18nDrafts =
                                        cmsDraftOverrideService.findComponentI18nDrafts(publishedComponentIds, lang);
                        for (Long componentId : publishedComponentIds) {
                                var draft = i18nDrafts.get(cmsDraftOverrideService.i18nKey(componentId, lang));
                                if (draft == null) {
                                        continue;
                                }
                                ComponentI18n i18n = componentI18nMap.get(componentId);
                                if (i18n == null) {
                                        i18n = new ComponentI18n();
                                        i18n.setComponentId(componentId);
                                        i18n.setLanguage(lang);
                                        componentI18nMap.put(componentId, i18n);
                                }
                                cmsDraftOverrideService.apply(i18n, draft);
                        }
                }

                List<Long> typeIds = componentMap.values().stream()
                                .map(Component::getComponentTypeId)
                                .filter(id -> id != null)
                                .distinct()
                                .toList();

                Map<Long, ComponentType> typeMap = typeIds.isEmpty()
                                ? Map.of()
                                : componentTypeRepository.findByIdIn(typeIds).stream()
                                                .collect(Collectors.toMap(ComponentType::getId, t -> t));

                Map<Long, List<ComponentEntry>> entriesByComponentId = publishedComponentIds.isEmpty()
                                ? Map.of()
                                : componentEntryRepository
                                                .findByComponentIdInAndStatusInOrderBySortOrder(publishedComponentIds,
                                                                visibleComponentStatuses)
                                                .stream()
                                                .map(this::cloneComponentEntry)
                                                .collect(Collectors.collectingAndThen(Collectors.toList(), this::groupEntriesByComponentId));

                List<Long> entryIds = entriesByComponentId.values().stream()
                                .flatMap(List::stream)
                                .map(ComponentEntry::getId)
                                .toList();

                if (cmsRequestContext.isPreview() && !entryIds.isEmpty()) {
                        Map<Long, com.backend.application.cms.preview.ComponentEntryDraftPayload> entryDrafts =
                                        cmsDraftOverrideService.findComponentEntryDrafts(entryIds);
                        entriesByComponentId.values().stream()
                                        .flatMap(List::stream)
                                        .forEach(entry -> cmsDraftOverrideService.apply(entry, entryDrafts.get(entry.getId())));
                        entriesByComponentId = groupEntriesByComponentId(entriesByComponentId.values().stream()
                                        .flatMap(List::stream)
                                        .toList());
                }

                // Batch fetch responsive media sets with eagerly loaded media and translations
                List<Long> responsiveMediaIds = componentMap.values().stream()
                                .map(Component::getResponsiveMedia)
                                .filter(r -> r != null)
                                .map(ResponsiveMediaSet::getId)
                                .distinct()
                                .toList();
                Map<Long, ResponsiveMediaSet> responsiveMediaMap = responsiveMediaIds.isEmpty()
                                ? Map.of()
                                : responsiveMediaSetRepository.findByIdInWithMedia(responsiveMediaIds).stream()
                                                .collect(Collectors.toMap(ResponsiveMediaSet::getId, r -> r));
                Map<Long, ComponentEntryI18n> entryI18nMap = entryIds.isEmpty()
                                ? new LinkedHashMap<>()
                                : new LinkedHashMap<>(componentEntryI18nRepository.findByEntryIdInAndLanguage(entryIds, lang)
                                                .stream()
                                                .map(this::cloneComponentEntryI18n)
                                                .collect(Collectors.toMap(ComponentEntryI18n::getEntryId, i -> i)));

                if (cmsRequestContext.isPreview() && !entryIds.isEmpty()) {
                        Map<String, com.backend.application.cms.preview.ComponentEntryI18nDraftPayload> entryI18nDrafts =
                                        cmsDraftOverrideService.findComponentEntryI18nDrafts(entryIds, lang);
                        for (Long entryId : entryIds) {
                                var draft = entryI18nDrafts.get(cmsDraftOverrideService.i18nKey(entryId, lang));
                                if (draft == null) {
                                        continue;
                                }
                                ComponentEntryI18n i18n = entryI18nMap.get(entryId);
                                if (i18n == null) {
                                        i18n = new ComponentEntryI18n();
                                        i18n.setEntryId(entryId);
                                        i18n.setLanguage(lang);
                                        entryI18nMap.put(entryId, i18n);
                                }
                                cmsDraftOverrideService.apply(i18n, draft);
                        }
                }

                // Batch-fetch all navigation nodes to avoid N+1 per component
                Set<Long> navIds = componentMap.values().stream()
                                .map(Component::getNavigationNodeId)
                                .filter(id -> id != null)
                                .collect(Collectors.toSet());
                Map<Long, NavigationDeliveryResponse> navigationMap = navIds.isEmpty()
                                ? Map.of()
                                : navigationService.getNavigationsByIds(navIds, lang);

                // Build all slot components once, reuse for both outputs
                Map<String, List<ComponentDeliveryResponse>> slotComponentsMap = buildAllSlotComponents(
                                activeSlots, componentsBySlotId, componentMap, componentI18nMap, typeMap,
                                responsiveMediaMap, entriesByComponentId, entryI18nMap, navigationMap, lang);

                Map<String, List<ComponentDeliveryResponse>> slotsMap = buildSlotsMap(activeSlots, slotComponentsMap);

                ContentSlotsWrapper contentSlots = buildContentSlots(activeSlots, slotComponentsMap);

                PageI18n i18n = i18nOpt.orElse(null);

                String typeCode = resolveTypeCode(page.getPageType());
                RobotTag robotTag = page.getRobotTag() != null ? page.getRobotTag() : RobotTag.INDEX_FOLLOW;

                return new PageDeliveryResponse(
                                page.getUid(),
                                i18n != null ? i18n.getName() : null,
                                i18n != null ? i18n.getTitle() : null,
                                i18n != null ? i18n.getDescription() : null,
                                robotTag.name(),
                                i18n != null ? i18n.getCanonicalUrl() : null,
                                page.getStyleClasses(),
                                templateUid,
                                typeCode,
                                code,
                                contentSlots,
                                slotsMap);
        }

        private Map<String, List<ComponentDeliveryResponse>> buildAllSlotComponents(
                        List<PageSlot> slots,
                        Map<Long, List<SlotComponent>> componentsBySlotId,
                        Map<Long, Component> componentMap,
                        Map<Long, ComponentI18n> componentI18nMap,
                        Map<Long, ComponentType> typeMap,
                        Map<Long, ResponsiveMediaSet> responsiveMediaMap,
                        Map<Long, List<ComponentEntry>> entriesByComponentId,
                        Map<Long, ComponentEntryI18n> entryI18nMap,
                        Map<Long, NavigationDeliveryResponse> navigationMap,
                        Language lang) {

                Map<String, List<ComponentDeliveryResponse>> result = new LinkedHashMap<>();
                for (PageSlot slot : slots) {
                        List<ComponentDeliveryResponse> compResponses = buildSlotComponents(
                                        slot, componentsBySlotId, componentMap, componentI18nMap, typeMap,
                                        responsiveMediaMap, entriesByComponentId, entryI18nMap, navigationMap, lang);
                        result.put(slot.getSlotName(), compResponses);
                }
                return result;
        }

        private Map<Long, List<ComponentEntry>> groupEntriesByComponentId(List<ComponentEntry> entries) {
                return entries.stream()
                                .sorted(Comparator.comparingInt(entry -> entry.getSortOrder() != null ? entry.getSortOrder() : 0))
                                .collect(Collectors.groupingBy(ComponentEntry::getComponentId, LinkedHashMap::new, Collectors.toList()));
        }

        private Map<String, List<ComponentDeliveryResponse>> buildSlotsMap(
                        List<PageSlot> slots,
                        Map<String, List<ComponentDeliveryResponse>> slotComponentsMap) {

                Map<String, List<ComponentDeliveryResponse>> slotsMap = new LinkedHashMap<>();
                for (PageSlot slot : slots) {
                        List<ComponentDeliveryResponse> compResponses = slotComponentsMap.getOrDefault(slot.getSlotName(), List.of());
                        if (!compResponses.isEmpty()) {
                                slotsMap.put(slot.getSlotName(), compResponses);
                        }
                }
                return slotsMap;
        }

        private List<ComponentDeliveryResponse> buildSlotComponents(
                        PageSlot slot,
                        Map<Long, List<SlotComponent>> componentsBySlotId,
                        Map<Long, Component> componentMap,
                        Map<Long, ComponentI18n> componentI18nMap,
                        Map<Long, ComponentType> typeMap,
                        Map<Long, ResponsiveMediaSet> responsiveMediaMap,
                        Map<Long, List<ComponentEntry>> entriesByComponentId,
                        Map<Long, ComponentEntryI18n> entryI18nMap,
                        Map<Long, NavigationDeliveryResponse> navigationMap,
                        Language lang) {

                return componentsBySlotId.getOrDefault(slot.getId(), List.of()).stream()
                                .filter(sc -> Boolean.TRUE.equals(sc.getIsVisible()))
                                .sorted(Comparator
                                                .comparingInt(sc -> sc.getSortOrder() != null ? sc.getSortOrder() : 0))
                                .filter(sc -> componentMap.containsKey(sc.getComponentId()))
                                .map(sc -> buildComponentResponse(sc, componentMap, componentI18nMap, typeMap,
                                                responsiveMediaMap, entriesByComponentId, entryI18nMap, navigationMap, lang))
                                .toList();
        }

        private ComponentDeliveryResponse buildComponentResponse(
                        SlotComponent sc,
                        Map<Long, Component> componentMap,
                        Map<Long, ComponentI18n> componentI18nMap,
                        Map<Long, ComponentType> typeMap,
                        Map<Long, ResponsiveMediaSet> responsiveMediaMap,
                        Map<Long, List<ComponentEntry>> entriesByComponentId,
                        Map<Long, ComponentEntryI18n> entryI18nMap,
                        Map<Long, NavigationDeliveryResponse> navigationMap,
                        Language lang) {

                Component comp = componentMap.get(sc.getComponentId());
                ComponentI18n compI18n = componentI18nMap.get(comp.getId());
                ComponentType type = comp.getComponentTypeId() != null
                                ? typeMap.get(comp.getComponentTypeId())
                                : null;

                List<ComponentEntry> entries = entriesByComponentId.getOrDefault(comp.getId(), List.of());
                List<EntryDeliveryResponse> entryResponses = entries.stream()
                                .map(entry -> buildEntryResponseOptimized(entry, entryI18nMap.get(entry.getId()),
                                                comp.getComponentTypeId(), lang))
                                .toList();

                // Build responsive media for component using the pre-fetched map
                ResponsiveMediaDeliveryResponse responsive = null;
                ResponsiveMediaSet responsiveMedia = comp.getResponsiveMedia() != null
                                ? responsiveMediaMap.get(comp.getResponsiveMedia().getId())
                                : null;
                if (responsiveMedia != null) {
                        responsive = responsiveMediaService.toDeliveryResponse(responsiveMedia, lang);
                }

                return new ComponentDeliveryResponse(
                                comp.getUid(),
                                type != null ? (type.getUid() != null ? type.getUid() : type.getName()) : null,
                                type != null ? type.getCategory() : null,
                                compI18n != null ? compI18n.getTitle() : null,
                                compI18n != null ? compI18n.getSubtitle() : null,
                                compI18n != null ? compI18n.getDescription() : null,
                                comp.getIsVisible(),
                                comp.getStyleClasses(),
                                buildComponentCustomFields(comp, comp.getComponentTypeId(), lang),
                                resolveNavigationType(type, comp),
                                resolveSearchBox(type, comp),
                                resolveNavigationNode(type, comp, navigationMap),
                                responsive,
                                entryResponses);
        }

        private EntryDeliveryResponse buildEntryResponseOptimized(ComponentEntry entry, ComponentEntryI18n i18n,
                        Long componentTypeId, Language lang) {
                Map<String, Object> customFields = new HashMap<>();
                if (i18n != null && i18n.getCustomData() != null) {
                        customFields.putAll(mediaFieldExpander.parseCustomData(i18n.getCustomData()));
                }
                customFields.keySet().removeAll(RESERVED_FIELDS);

                // Expand MEDIA fields to full responsive media objects
                if (!customFields.isEmpty() && componentTypeId != null) {
                        customFields = mediaFieldExpander.expandMediaFields(customFields, componentTypeId, lang);
                }

                ResponsiveMediaDeliveryResponse responsive = null;
                if (entry.getResponsiveMedia() != null) {
                        responsive = responsiveMediaService.toDeliveryResponse(entry.getResponsiveMedia(), lang);
                }

                return EntryDeliveryResponse.builder()
                                .uid(entry.getUid())
                                .order(entry.getSortOrder())
                                .title(i18n != null ? i18n.getTitle() : null)
                                .description(i18n != null ? i18n.getDescription() : null)
                                .isVisible(entry.getIsVisible())
                                .styleClasses(entry.getStyleClasses())
                                .isExternal(UrlUtils.computeIsExternal(customFields))
                                .responsive(responsive)
                                .customFields(customFields.isEmpty() ? null : customFields)
                                .build();
        }

        private NavigationType resolveNavigationType(ComponentType type, Component component) {
                if (!isNavigationAware(type)) {
                        return null;
                }
                return component.getNavigationType();
        }

        private Map<String, Object> buildComponentCustomFields(Component component, Long componentTypeId, Language lang) {
                Map<String, Object> customFields = new LinkedHashMap<>();
                if (component.getCustomData() != null && !component.getCustomData().isBlank()) {
                        customFields.putAll(mediaFieldExpander.parseCustomData(component.getCustomData()));
                }

                if (!customFields.isEmpty() && componentTypeId != null) {
                        customFields = mediaFieldExpander.expandMediaFields(customFields, componentTypeId, lang);
                }

                return customFields.isEmpty() ? null : customFields;
        }

        private Boolean resolveSearchBox(ComponentType type, Component component) {
                if (!isNavigationAware(type)) {
                        return null;
                }
                return component.getSearchBox();
        }

        private NavigationDeliveryResponse resolveNavigationNode(ComponentType type, Component component,
                        Map<Long, NavigationDeliveryResponse> navigationMap) {
                if (!isNavigationAware(type) || component.getNavigationNodeId() == null) {
                        return null;
                }
                return navigationMap.get(component.getNavigationNodeId());
        }

        private boolean isNavigationAware(ComponentType type) {
                return type != null && type.isNavigationAware();
        }

        private List<PageSlot> resolveEffectiveSlotsForDelivery(
                        Page page,
                        List<PageSlot> pageSlots,
                        List<PageSlot> sharedSlots) {
                if (page.getTemplateId() == null) {
                        return mergeSlotsWithoutTemplate(pageSlots, sharedSlots);
                }

                List<TemplateSlot> templateSlots = templateSlotRepository.findByTemplateId(page.getTemplateId());
                if (templateSlots.isEmpty()) {
                        return mergeSlotsWithoutTemplate(pageSlots, sharedSlots);
                }

                Map<String, PageSlot> pageBySlotName = pageSlots.stream()
                                .collect(Collectors.toMap(PageSlot::getSlotName, slot -> slot, (first, second) -> first));
                Map<String, PageSlot> sharedBySlotName = sharedSlots.stream()
                                .collect(Collectors.toMap(PageSlot::getSlotName, slot -> slot, (first, second) -> first));

                List<PageSlot> effective = new ArrayList<>();
                Set<String> templateSlotNames = new LinkedHashSet<>();
                for (TemplateSlot templateSlot : templateSlots) {
                        templateSlotNames.add(templateSlot.getSlotName());
                        PageSlot sharedSource = sharedBySlotName.get(templateSlot.getSlotName());
                        PageSlot source = sharedSource != null
                                        // Shared slot exists → use it so component bindings are always resolved correctly.
                                        // This prevents accidentally-created page-specific slots from shadowing shared chrome slots.
                                        ? sharedSource
                                        : pageBySlotName.get(templateSlot.getSlotName());
                        effective.add(buildEffectiveSlot(page.getId(), templateSlot, source));
                }

                List<PageSlot> extraSlots = mergeSlotsWithoutTemplate(
                                pageSlots.stream()
                                                .filter(slot -> !templateSlotNames.contains(slot.getSlotName()))
                                                .toList(),
                                sharedSlots.stream()
                                                .filter(slot -> !templateSlotNames.contains(slot.getSlotName()))
                                                .toList());

                effective.addAll(extraSlots);

                return effective.stream()
                                .sorted(Comparator
                                                .comparingInt((PageSlot slot) -> slot.getSortOrder() != null
                                                                ? slot.getSortOrder()
                                                                : 0)
                                                .thenComparing(PageSlot::getSlotName, String.CASE_INSENSITIVE_ORDER))
                                .toList();
        }

        private List<PageSlot> mergeSlotsWithoutTemplate(List<PageSlot> pageSlots, List<PageSlot> sharedSlots) {
                Map<String, PageSlot> bySlotName = new LinkedHashMap<>();
                for (PageSlot shared : sharedSlots) {
                        bySlotName.putIfAbsent(shared.getSlotName(), shared);
                }
                for (PageSlot pageSlot : pageSlots) {
                        bySlotName.put(pageSlot.getSlotName(), pageSlot);
                }
                return bySlotName.values().stream()
                                .sorted(Comparator
                                                .comparingInt((PageSlot slot) -> slot.getSortOrder() != null
                                                                ? slot.getSortOrder()
                                                                : 0)
                                                .thenComparing(PageSlot::getSlotName, String.CASE_INSENSITIVE_ORDER))
                                .toList();
        }

        private PageSlot buildEffectiveSlot(Long pageId, TemplateSlot templateSlot, PageSlot source) {
                PageSlot effective = new PageSlot();
                if (source != null) {
                        effective.setId(source.getId());
                        effective.setUuid(source.getUuid());
                        effective.setUid(source.getUid());
                        effective.setCreatedAt(source.getCreatedAt());
                        effective.setUpdatedAt(source.getUpdatedAt());
                        effective.setCreatedBy(source.getCreatedBy());
                        effective.setUpdatedBy(source.getUpdatedBy());
                        effective.setIsActive(source.getIsActive());
                        effective.setIsShared(source.getIsShared());
                } else {
                        effective.setUid("template-slot-" + pageId + "-" + templateSlot.getId());
                        effective.setIsActive(true);
                        effective.setIsShared(false);
                }

                effective.setPageId(pageId);
                effective.setSlotName(templateSlot.getSlotName());
                effective.setPosition(templateSlot.getPosition());
                effective.setSortOrder(templateSlot.getSortOrder());
                return effective;
        }

        private Component cloneComponent(Component source) {
                Component copy = new Component();
                copy.setId(source.getId());
                copy.setUuid(source.getUuid());
                copy.setUid(source.getUid());
                copy.setComponentTypeId(source.getComponentTypeId());
                copy.setName(source.getName());
                copy.setDisplayOrder(source.getDisplayOrder());
                copy.setIsVisible(source.getIsVisible());
                copy.setStyleClasses(source.getStyleClasses());
                copy.setCustomData(source.getCustomData());
                copy.setStatus(source.getStatus());
                copy.setResponsiveMedia(source.getResponsiveMedia());
                copy.setNavigationNodeId(source.getNavigationNodeId());
                copy.setNavigationType(source.getNavigationType());
                copy.setSearchBox(source.getSearchBox());
                return copy;
        }

        private ComponentI18n cloneComponentI18n(ComponentI18n source) {
                ComponentI18n copy = new ComponentI18n();
                copy.setId(source.getId());
                copy.setUuid(source.getUuid());
                copy.setUid(source.getUid());
                copy.setComponentId(source.getComponentId());
                copy.setLanguage(source.getLanguage());
                copy.setTitle(source.getTitle());
                copy.setSubtitle(source.getSubtitle());
                copy.setDescription(source.getDescription());
                copy.setStatus(source.getStatus());
                return copy;
        }

        private ComponentEntry cloneComponentEntry(ComponentEntry source) {
                ComponentEntry copy = new ComponentEntry();
                copy.setId(source.getId());
                copy.setUuid(source.getUuid());
                copy.setUid(source.getUid());
                copy.setComponentId(source.getComponentId());
                copy.setSortOrder(source.getSortOrder());
                copy.setIsVisible(source.getIsVisible());
                copy.setStyleClasses(source.getStyleClasses());
                copy.setStatus(source.getStatus());
                copy.setResponsiveMedia(source.getResponsiveMedia());
                return copy;
        }

        private ComponentEntryI18n cloneComponentEntryI18n(ComponentEntryI18n source) {
                ComponentEntryI18n copy = new ComponentEntryI18n();
                copy.setId(source.getId());
                copy.setUuid(source.getUuid());
                copy.setUid(source.getUid());
                copy.setEntryId(source.getEntryId());
                copy.setLanguage(source.getLanguage());
                copy.setTitle(source.getTitle());
                copy.setDescription(source.getDescription());
                copy.setCustomData(source.getCustomData());
                copy.setStatus(source.getStatus());
                return copy;
        }


        private ContentSlotsWrapper buildContentSlots(
                        List<PageSlot> slots,
                        Map<String, List<ComponentDeliveryResponse>> slotComponentsMap) {

                List<ContentSlotDeliveryResponse> contentSlotList = new ArrayList<>();

                for (PageSlot slot : slots) {
                        List<ComponentDeliveryResponse> compResponses = slotComponentsMap.getOrDefault(slot.getSlotName(), List.of());

                        ContentSlotDeliveryResponse contentSlot = new ContentSlotDeliveryResponse(
                                        slot.getSlotName() + "Slot",
                                        slot.getUuid(),
                                        slot.getPosition(),
                                        slot.getSlotName() + " Content Slot",
                                        Boolean.TRUE.equals(slot.getIsShared()),
                                        ContentSlotDeliveryResponse.ComponentsWrapper.of(compResponses));

                        contentSlotList.add(contentSlot);
                }

                return ContentSlotsWrapper.of(contentSlotList);
        }
}
