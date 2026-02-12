package com.backend.application.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.delivery.ComponentDeliveryResponse;
import com.backend.application.dto.delivery.ContentSlotDeliveryResponse;
import com.backend.application.dto.delivery.ContentSlotsWrapper;
import com.backend.application.dto.delivery.EntryDeliveryResponse;
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
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PageDeliveryServiceImpl implements PageDeliveryService {

        private static final Set<String> RESERVED_FIELDS = Set.of(
                        "uid", "order", "title", "description", "isVisible", "styleClasses");

        private final PageRepository pageRepository;
        private final PageI18nRepository pageI18nRepository;
        private final PageSlotRepository pageSlotRepository;
        private final PageTemplateRepository pageTemplateRepository;
        private final SlotComponentRepository slotComponentRepository;
        private final ComponentRepository componentRepository;
        private final ComponentTypeRepository componentTypeRepository;
        private final ComponentI18nRepository componentI18nRepository;
        private final ComponentEntryRepository componentEntryRepository;
        private final ComponentEntryI18nRepository componentEntryI18nRepository;
        private final ResponsiveMediaSetRepository responsiveMediaSetRepository;
        private final ResponsiveMediaService responsiveMediaService;
        private final MediaFieldExpander mediaFieldExpander;

        @Override
        public Optional<PageDeliveryResponse> resolvePageForDelivery(String pageType, String pageLabelOrId,
                        String code, Language lang) {
                Optional<Page> pageOpt;
                String normalizedPageLabelOrId = normalizePageLabelOrId(pageLabelOrId);
                String normalizedCode = normalizeParam(code);
                final String[] codeToInclude = { null };

                if (pageType == null || pageType.isBlank()) {
                        pageOpt = resolveHomepage();
                } else {
                        Optional<PageType> resolvedType = PageType.fromTypeCode(pageType);
                        if (resolvedType.isEmpty()) {
                                pageOpt = Optional.empty();
                        } else {
                                switch (resolvedType.get()) {
                                        case CONTENT -> pageOpt = normalizedPageLabelOrId != null
                                                        ? resolvePublishedPageByCanonicalUrl(lang, normalizedPageLabelOrId, PageType.CONTENT)
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
                                                        ? resolvePublishedPageByCanonicalUrl(lang, normalizedPageLabelOrId, PageType.LANDING)
                                                        : Optional.empty();
                                        default -> pageOpt = Optional.empty();
                                }
                        }
                }

                return pageOpt.map(page -> buildPageDeliveryResponse(page, lang, codeToInclude[0]));
        }

        private Optional<Page> resolveHomepage() {
                long publishedHomeCount = pageRepository.countByIsHomeTrueAndStatus(PageStatus.PUBLISHED);
                if (publishedHomeCount > 1) {
                        log.warn("Multiple published homepages found (count={}), first one will be used",
                                        publishedHomeCount);
                }
                return pageRepository.findByIsHomeTrueAndStatus(PageStatus.PUBLISHED);
        }

        private Optional<Page> resolveUniqueTemplatePage(PageType pageType) {
                long publishedTypeCount = pageRepository.countByPageTypeAndStatus(pageType, PageStatus.PUBLISHED);
                if (publishedTypeCount > 1) {
                        log.warn("Multiple published {} templates found (count={}), lowest id will be used", pageType,
                                        publishedTypeCount);
                }
                return pageRepository.findFirstByPageTypeAndStatusOrderByIdAsc(pageType, PageStatus.PUBLISHED);
        }

        private Optional<Page> resolvePublishedPageByCanonicalUrl(Language lang, String canonicalUrl, PageType pageType) {
                return pageI18nRepository.findPublishedByCanonicalUrl(lang, canonicalUrl)
                                .flatMap(i18n -> pageRepository.findByIdAndStatus(i18n.getPageId(), PageStatus.PUBLISHED))
                                .filter(page -> pageType == page.getPageType());
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

                List<PageSlot> allSlots = new ArrayList<>(sharedSlots);
                allSlots.addAll(pageSlots);

                List<PageSlot> activeSlots = allSlots.stream()
                                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                                .toList();

                List<Long> slotIds = activeSlots.stream()
                                .map(PageSlot::getId)
                                .toList();

                List<SlotComponent> allSlotComponents = slotComponentRepository.findBySlotIdIn(slotIds);
                Map<Long, List<SlotComponent>> componentsBySlotId = allSlotComponents.stream()
                                .collect(Collectors.groupingBy(SlotComponent::getSlotId));

                List<Long> allComponentIds = allSlotComponents.stream()
                                .map(SlotComponent::getComponentId)
                                .distinct()
                                .toList();

                Map<Long, Component> componentMap = allComponentIds.isEmpty()
                                ? Map.of()
                                : componentRepository.findByIdIn(allComponentIds).stream()
                                                .filter(c -> c.getStatus() == ComponentStatus.PUBLISHED)
                                                .collect(Collectors.toMap(Component::getId, c -> c));

                List<Long> publishedComponentIds = componentMap.keySet().stream().toList();

                Map<Long, ComponentI18n> componentI18nMap = publishedComponentIds.isEmpty()
                                ? Map.of()
                                : componentI18nRepository.findByComponentIdInAndLanguage(publishedComponentIds, lang)
                                                .stream()
                                                .collect(Collectors.toMap(ComponentI18n::getComponentId, i -> i));

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
                                                .findByComponentIdInAndStatusOrderBySortOrder(publishedComponentIds,
                                                                ComponentStatus.PUBLISHED)
                                                .stream()
                                                .collect(Collectors.groupingBy(ComponentEntry::getComponentId));

                List<Long> entryIds = entriesByComponentId.values().stream()
                                .flatMap(List::stream)
                                .map(ComponentEntry::getId)
                                .toList();

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
                                ? Map.of()
                                : componentEntryI18nRepository.findByEntryIdInAndLanguage(entryIds, lang)
                                                .stream()
                                                .collect(Collectors.toMap(ComponentEntryI18n::getEntryId, i -> i));

                Map<String, List<ComponentDeliveryResponse>> slotsMap = buildSlotsMap(
                                activeSlots, componentsBySlotId, componentMap, componentI18nMap, typeMap,
                                responsiveMediaMap,
                                entriesByComponentId, entryI18nMap, lang);

                ContentSlotsWrapper contentSlots = buildContentSlots(
                                activeSlots, componentsBySlotId, componentMap, componentI18nMap, typeMap,
                                responsiveMediaMap,
                                entriesByComponentId, entryI18nMap, lang);

                PageI18n i18n = i18nOpt.orElse(null);

        String typeCode = resolveTypeCode(page.getPageType());
        RobotTag robotTag = page.getRobotTag() != null ? page.getRobotTag() : RobotTag.INDEX_FOLLOW;

                return PageDeliveryResponse.builder()
                                .uid(page.getUid())
                                .name(i18n != null ? i18n.getName() : null)
                                .title(i18n != null ? i18n.getTitle() : null)
                                .description(i18n != null ? i18n.getDescription() : null)
                                .robotTag(robotTag.name())
                                .canonicalUrl(i18n != null ? i18n.getCanonicalUrl() : null)
                                .styleClasses(page.getStyleClasses())
                                .template(templateUid)
                                .typeCode(typeCode)
                                .code(code)
                                .contentSlots(contentSlots)
                                .slots(slotsMap)
                                .build();
        }

        private Map<String, List<ComponentDeliveryResponse>> buildSlotsMap(
                        List<PageSlot> slots,
                        Map<Long, List<SlotComponent>> componentsBySlotId,
                        Map<Long, Component> componentMap,
                        Map<Long, ComponentI18n> componentI18nMap,
                        Map<Long, ComponentType> typeMap,
                        Map<Long, ResponsiveMediaSet> responsiveMediaMap,
                        Map<Long, List<ComponentEntry>> entriesByComponentId,
                        Map<Long, ComponentEntryI18n> entryI18nMap,
                        Language lang) {

                Map<String, List<ComponentDeliveryResponse>> slotsMap = new LinkedHashMap<>();

                for (PageSlot slot : slots) {
                        List<ComponentDeliveryResponse> compResponses = buildSlotComponents(
                                        slot, componentsBySlotId, componentMap, componentI18nMap, typeMap,
                                        responsiveMediaMap, entriesByComponentId, entryI18nMap, lang);

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
                        Language lang) {

                return componentsBySlotId.getOrDefault(slot.getId(), List.of()).stream()
                                .filter(sc -> Boolean.TRUE.equals(sc.getIsVisible()))
                                .sorted(Comparator
                                                .comparingInt(sc -> sc.getSortOrder() != null ? sc.getSortOrder() : 0))
                                .filter(sc -> componentMap.containsKey(sc.getComponentId()))
                                .map(sc -> buildComponentResponse(sc, componentMap, componentI18nMap, typeMap,
                                                responsiveMediaMap, entriesByComponentId, entryI18nMap, lang))
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

                return ComponentDeliveryResponse.builder()
                                .uid(comp.getUid())
                                .type(type != null ? type.getName() : null)
                                .category(type != null ? type.getCategory() : null)
                                .title(compI18n != null ? compI18n.getTitle() : null)
                                .subtitle(compI18n != null ? compI18n.getSubtitle() : null)
                                .description(compI18n != null ? compI18n.getDescription() : null)
                                .isVisible(comp.getIsVisible())
                                .styleClasses(comp.getStyleClasses())
                                .responsive(responsive)
                                .entries(entryResponses)
                                .build();
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

                return EntryDeliveryResponse.builder()
                                .uid(entry.getUid())
                                .order(entry.getSortOrder())
                                .title(i18n != null ? i18n.getTitle() : null)
                                .description(i18n != null ? i18n.getDescription() : null)
                                .isVisible(entry.getIsVisible())
                                .styleClasses(entry.getStyleClasses())
                                .customFields(customFields.isEmpty() ? null : customFields)
                                .build();
        }

        /**
         * Builds Hybris-compatible contentSlots wrapper with structured slot metadata.
         */
        private ContentSlotsWrapper buildContentSlots(
                        List<PageSlot> slots,
                        Map<Long, List<SlotComponent>> componentsBySlotId,
                        Map<Long, Component> componentMap,
                        Map<Long, ComponentI18n> componentI18nMap,
                        Map<Long, ComponentType> typeMap,
                        Map<Long, ResponsiveMediaSet> responsiveMediaMap,
                        Map<Long, List<ComponentEntry>> entriesByComponentId,
                        Map<Long, ComponentEntryI18n> entryI18nMap,
                        Language lang) {

                List<ContentSlotDeliveryResponse> contentSlotList = new ArrayList<>();

                for (PageSlot slot : slots) {
                        List<ComponentDeliveryResponse> compResponses = buildSlotComponents(
                                        slot, componentsBySlotId, componentMap, componentI18nMap, typeMap,
                                        responsiveMediaMap, entriesByComponentId, entryI18nMap, lang);

                        ContentSlotDeliveryResponse contentSlot = ContentSlotDeliveryResponse.builder()
                                        .slotId(slot.getSlotName() + "Slot")
                                        .slotUuid(slot.getUuid())
                                        .position(slot.getSlotName())
                                        .name(slot.getSlotName() + " Content Slot")
                                        .slotShared(Boolean.TRUE.equals(slot.getIsShared()))
                                        .components(ContentSlotDeliveryResponse.ComponentsWrapper.of(compResponses))
                                        .build();

                        contentSlotList.add(contentSlot);
                }

                return ContentSlotsWrapper.of(contentSlotList);
        }
}
