package com.backend.application.cms.preview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.MessageSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.application.service.ComponentMediaLinkSyncService;
import com.backend.application.service.SiteActivityPublisher;
import com.backend.domain.entity.CmsDraftOverride;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentEntry;
import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.entity.Media;
import com.backend.domain.entity.PageSlot;
import com.backend.domain.entity.ResponsiveMediaSet;
import com.backend.domain.entity.SlotComponent;
import com.backend.domain.enums.CmsDraftTargetType;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
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

@ExtendWith(MockitoExtension.class)
class CmsDraftOverrideServiceLanguageTest {

  @Mock
  private CmsDraftOverrideRepository draftOverrideRepository;
  @Mock
  private ComponentRepository componentRepository;
  @Mock
  private ComponentI18nRepository componentI18nRepository;
  @Mock
  private ComponentEntryRepository componentEntryRepository;
  @Mock
  private ComponentEntryI18nRepository componentEntryI18nRepository;
  @Mock
  private PageSlotRepository pageSlotRepository;
  @Mock
  private SlotComponentRepository slotComponentRepository;
  @Mock
  private ResponsiveMediaSetRepository responsiveMediaSetRepository;
  @Mock
  private ComponentTypeRepository componentTypeRepository;
  @Mock
  private NavigationNodeRepository navigationNodeRepository;
  @Mock
  private ComponentMediaLinkSyncService componentMediaLinkSyncService;
  @Mock
  private SiteActivityPublisher siteActivityPublisher;
  @Mock
  private MessageSource messageSource;

  private CmsDraftOverrideService service;

  @BeforeEach
  void setUp() {
    service = new CmsDraftOverrideService(
        draftOverrideRepository,
        componentRepository,
        componentI18nRepository,
        componentEntryRepository,
        componentEntryI18nRepository,
        pageSlotRepository,
        slotComponentRepository,
        responsiveMediaSetRepository,
        componentTypeRepository,
        navigationNodeRepository,
        componentMediaLinkSyncService,
        siteActivityPublisher,
        messageSource,
        new ObjectMapper());
  }

  @Test
  void findComponentI18nDraft_ShouldReturnEmptyWhenLanguageNull() {
    assertThat(service.findComponentI18nDraft(1L, null)).isEmpty();
  }

  @Test
  void findComponentI18nDrafts_ShouldReturnEmptyMapWhenLanguageNull() {
    assertThat(service.findComponentI18nDrafts(List.of(1L, 2L), null)).isEmpty();
  }

  @Test
  void listPageDrafts_ShouldReturnBackendComputedGroupsAndDisplayValues() throws Exception {
    setupMessages();
    setupPageGraph();
    setupDraftRows();
    setupExistingContent();

    SmartEditDraftOverviewResponse response = service.listPageDrafts(10L, Language.TR, Locale.ENGLISH);

    assertThat(response.count()).isEqualTo(4);
    assertThat(response.groupCount()).isEqualTo(1);
    assertThat(response.groups()).hasSize(1);
    SmartEditDraftGroupResponse group = response.groups().getFirst();
    assertThat(group.key()).isEqualTo("component:100");
    assertThat(group.draftIds()).containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
    assertThat(group.title()).isEqualTo("Hero");
    assertThat(group.fields()).extracting(SmartEditDraftFieldChange::label)
        .contains("Name", "Title", "Sort order", "headline");

    SmartEditDraftFieldChange media = group.fields().stream()
        .filter(SmartEditDraftFieldChange::isMedia)
        .findFirst()
        .orElseThrow();
    assertThat(media.beforeText()).isEqualTo("old-set");
    assertThat(media.afterText()).isEqualTo("new-set");
    assertThat(media.mediaAfter()).extracting(SmartEditMediaPreviewResponse::label)
        .contains("Desktop: desktop.jpg", "Mobile: mobile.jpg");
    assertThat(media.mediaAfter()).extracting(SmartEditMediaPreviewResponse::url)
        .contains("/api/media/files/desktop.jpg", "/api/media/files/mobile.jpg");
  }

  @Test
  void discardDraftGroup_ShouldDeleteOnlyMatchingPageScopedGroup() throws Exception {
    setupMessages();
    setupPageGraph(true);
    setupDraftRows(true);
    setupExistingContent(true);

    int deletedCount = service.discardDraftGroup(10L, Language.TR, "component:100", 99L);

    assertThat(deletedCount).isEqualTo(4);
    ArgumentCaptor<Iterable<CmsDraftOverride>> captor = ArgumentCaptor.forClass(Iterable.class);
    verify(draftOverrideRepository).deleteAll(captor.capture());
    assertThat(captor.getValue()).extracting("id").containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
  }

  private void setupMessages() {
    when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenAnswer(invocation -> switch (invocation.getArgument(0, String.class)) {
      case "cms.preview.field.name" -> "Name";
      case "cms.preview.field.title" -> "Title";
      case "cms.preview.field.sortOrder" -> "Sort order";
      case "cms.preview.field.responsiveMedia" -> "Responsive media";
      case "cms.preview.value.empty" -> "Empty";
      case "cms.preview.value.yes" -> "Yes";
      case "cms.preview.value.no" -> "No";
      case "cms.preview.value.desktop" -> "Desktop";
      case "cms.preview.value.mobile" -> "Mobile";
      default -> invocation.getArgument(0, String.class);
    });
  }

  private void setupPageGraph() {
    setupPageGraph(false);
  }

  private void setupPageGraph(boolean includeSecondComponent) {
    PageSlot slot = new PageSlot();
    slot.setId(50L);
    slot.setPageId(10L);
    slot.setIsShared(false);
    when(pageSlotRepository.findByPageId(10L)).thenReturn(List.of(slot));

    SlotComponent slotComponent = new SlotComponent();
    slotComponent.setSlotId(50L);
    slotComponent.setComponentId(100L);
    slotComponent.setIsVisible(true);
    if (includeSecondComponent) {
      SlotComponent secondSlotComponent = new SlotComponent();
      secondSlotComponent.setSlotId(50L);
      secondSlotComponent.setComponentId(200L);
      secondSlotComponent.setIsVisible(true);
      when(slotComponentRepository.findBySlotIdIn(List.of(50L))).thenReturn(List.of(slotComponent, secondSlotComponent));
    } else {
      when(slotComponentRepository.findBySlotIdIn(List.of(50L))).thenReturn(List.of(slotComponent));
    }

    ComponentEntry entry = entry(300L, 100L, 1);
    ComponentEntry secondEntry = entry(301L, 200L, 1);
    when(componentEntryRepository.findByComponentIdInAndStatusInOrderBySortOrder(any(), anyCollection()))
        .thenAnswer(invocation -> filterEntriesByComponentIds(
            includeSecondComponent ? List.of(entry, secondEntry) : List.of(entry),
            invocation.getArgument(0)));
  }

  private List<CmsDraftOverride> setupDraftRows() throws Exception {
    return setupDraftRows(false);
  }

  private List<CmsDraftOverride> setupDraftRows(boolean includeSecondGroup) throws Exception {
    List<CmsDraftOverride> componentDrafts = List.of(draft(1L, CmsDraftTargetType.COMPONENT, 100L, CmsDraftOverride.NO_LANGUAGE,
        new ComponentDraftPayload("Hero Draft", null, null, null, 700L, null, null, null)),
        draft(5L, CmsDraftTargetType.COMPONENT, 200L, CmsDraftOverride.NO_LANGUAGE,
            new ComponentDraftPayload("Banner Draft", null, null, null, null, null, null, null)));
    List<CmsDraftOverride> componentI18nDrafts = List.of(draft(2L, CmsDraftTargetType.COMPONENT_I18N, 100L, Language.TR.name(),
        new ComponentI18nDraftPayload("Yeni başlık", true, null, false, null, false)),
        draft(6L, CmsDraftTargetType.COMPONENT_I18N, 100L, Language.EN.name(),
            new ComponentI18nDraftPayload("English title", true, null, false, null, false)));
    List<CmsDraftOverride> entryDrafts = List.of(draft(3L, CmsDraftTargetType.COMPONENT_ENTRY, 300L, CmsDraftOverride.NO_LANGUAGE,
        new ComponentEntryDraftPayload(2, null, null, null)));
    List<CmsDraftOverride> entryI18nDrafts = List.of(draft(4L, CmsDraftTargetType.COMPONENT_ENTRY_I18N, 300L, Language.TR.name(),
        new ComponentEntryI18nDraftPayload(null, false, null, false, java.util.Map.of("headline", "Dynamic title"), true)));

    List<CmsDraftOverride> visibleComponentDrafts = includeSecondGroup ? componentDrafts : List.of(componentDrafts.getFirst());
    when(draftOverrideRepository.findByTargetTypeAndTargetIdIn(eq(CmsDraftTargetType.COMPONENT), anyCollection()))
        .thenAnswer(invocation -> filterDraftsByTargetIds(visibleComponentDrafts, invocation.getArgument(1)));
    when(draftOverrideRepository.findByTargetTypeAndTargetIdIn(eq(CmsDraftTargetType.COMPONENT_I18N), anyCollection()))
        .thenAnswer(invocation -> filterDraftsByTargetIds(componentI18nDrafts, invocation.getArgument(1)));
    when(draftOverrideRepository.findByTargetTypeAndTargetIdIn(eq(CmsDraftTargetType.COMPONENT_ENTRY), anyCollection()))
        .thenAnswer(invocation -> filterDraftsByTargetIds(entryDrafts, invocation.getArgument(1)));
    when(draftOverrideRepository.findByTargetTypeAndTargetIdIn(eq(CmsDraftTargetType.COMPONENT_ENTRY_I18N), anyCollection()))
        .thenAnswer(invocation -> filterDraftsByTargetIds(entryI18nDrafts, invocation.getArgument(1)));

    return List.of(componentDrafts.getFirst(), componentI18nDrafts.getFirst(), entryDrafts.getFirst(), entryI18nDrafts.getFirst());
  }

  private void setupExistingContent() {
    setupExistingContent(false);
  }

  private void setupExistingContent(boolean includeSecondComponent) {
    Component component = component(100L, "Hero", "hero");
    Component secondComponent = component(200L, "Banner", "banner");
    ResponsiveMediaSet existingMedia = responsiveMediaSet(600L, "old-set", "old-desktop.jpg", "old-mobile.jpg");
    component.setResponsiveMedia(existingMedia);
    when(componentRepository.findByIdIn(any())).thenAnswer(invocation -> filterByComponentIds(
        includeSecondComponent ? List.of(component, secondComponent) : List.of(component),
        invocation.getArgument(0)));
    lenient().when(componentRepository.findById(100L)).thenReturn(Optional.of(component));
    lenient().when(componentRepository.findById(200L)).thenReturn(Optional.of(secondComponent));

    ComponentI18n componentI18n = new ComponentI18n();
    componentI18n.setComponentId(100L);
    componentI18n.setLanguage(Language.TR);
    componentI18n.setTitle("Eski başlık");
    when(componentI18nRepository.findByComponentIdInAndLanguage(any(), eq(Language.TR))).thenReturn(List.of(componentI18n));

    ComponentEntry entry = entry(300L, 100L, 1);
    when(componentEntryRepository.findByIdIn(any())).thenReturn(List.of(entry));
    lenient().when(componentEntryRepository.findById(300L)).thenReturn(Optional.of(entry));

    ComponentEntryI18n entryI18n = new ComponentEntryI18n();
    entryI18n.setEntryId(300L);
    entryI18n.setLanguage(Language.TR);
    entryI18n.setCustomData("{\"headline\":\"Old dynamic title\"}");
    when(componentEntryI18nRepository.findByEntryIdInAndLanguage(any(), eq(Language.TR))).thenReturn(List.of(entryI18n));

    when(responsiveMediaSetRepository.findById(700L))
        .thenReturn(Optional.of(responsiveMediaSet(700L, "new-set", "desktop.jpg", "mobile.jpg")));
    when(responsiveMediaSetRepository.findById(600L))
        .thenReturn(Optional.of(existingMedia));
  }

  private CmsDraftOverride draft(Long id, CmsDraftTargetType type, Long targetId, String languageKey, Object payload)
      throws Exception {
    CmsDraftOverride draft = new CmsDraftOverride();
    draft.setId(id);
    draft.setTargetType(type);
    draft.setTargetId(targetId);
    draft.setLanguageKey(languageKey);
    draft.setPayload(new ObjectMapper().writeValueAsString(payload));
    draft.setUpdatedAt(LocalDateTime.now().plusMinutes(id));
    return draft;
  }

  private Component component(Long id, String name, String uid) {
    Component component = new Component();
    component.setId(id);
    component.setName(name);
    component.setUid(uid);
    component.setComponentTypeId(1L);
    component.setStatus(ComponentStatus.PUBLISHED);
    return component;
  }

  private ComponentEntry entry(Long id, Long componentId, int sortOrder) {
    ComponentEntry entry = new ComponentEntry();
    entry.setId(id);
    entry.setUid("entry-" + id);
    entry.setComponentId(componentId);
    entry.setSortOrder(sortOrder);
    entry.setStatus(ComponentStatus.PUBLISHED);
    return entry;
  }

  private ResponsiveMediaSet responsiveMediaSet(Long id, String code, String desktopName, String mobileName) {
    ResponsiveMediaSet mediaSet = new ResponsiveMediaSet();
    mediaSet.setId(id);
    mediaSet.setCode(code);
    mediaSet.setDesktopMedia(media(1L, desktopName));
    mediaSet.setMobileMedia(media(2L, mobileName));
    return mediaSet;
  }

  private Media media(Long id, String originalName) {
    Media media = new Media();
    media.setId(id);
    media.setOriginalName(originalName);
    media.setFileName(originalName);
    return media;
  }

  private List<CmsDraftOverride> filterDraftsByTargetIds(List<CmsDraftOverride> drafts, Collection<Long> targetIds) {
    return drafts.stream()
        .filter(draft -> targetIds.contains(draft.getTargetId()))
        .toList();
  }

  private <T extends ComponentEntry> List<T> filterEntriesByComponentIds(List<T> entries, Collection<Long> componentIds) {
    return entries.stream()
        .filter(entry -> componentIds.contains(entry.getComponentId()))
        .toList();
  }

  private List<Component> filterByComponentIds(List<Component> components, Collection<Long> componentIds) {
    return components.stream()
        .filter(component -> componentIds.contains(component.getId()))
        .toList();
  }
}
