package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.transaction.PlatformTransactionManager;

import com.backend.application.dto.request.ComponentI18nUpdateCommand;
import com.backend.application.dto.request.UpdateComponentCompositeRequest;
import com.backend.application.dto.response.ComponentListItemResponse;
import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.entity.ComponentType;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentEntryI18nRepository;
import com.backend.domain.repository.ComponentEntryRepository;
import com.backend.domain.repository.ComponentI18nRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTypeRepository;
import com.backend.domain.repository.NavigationNodeRepository;
import com.backend.domain.repository.ResponsiveMediaSetRepository;
import com.backend.shared.common.SecurityHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ComponentServiceImplTest {

  @Mock
  private ComponentRepository componentRepository;

  @Mock
  private ComponentEntryRepository componentEntryRepository;

  @Mock
  private ComponentEntryI18nRepository componentEntryI18nRepository;

  @Mock
  private ComponentI18nRepository componentI18nRepository;

  @Mock
  private ComponentTypeService componentTypeService;

  @Mock
  private ComponentTypeRepository componentTypeRepository;

  @Mock
  private NavigationNodeRepository navigationNodeRepository;

  @Mock
  private ResponsiveMediaSetRepository responsiveMediaSetRepository;

  @Mock
  private ComponentMediaLinkSyncService componentMediaLinkSyncService;

  @Mock
  private SiteActivityPublisher activityPublisher;

  @Mock
  private SecurityHelper securityHelper;

  @Mock
  private MessageSource messageSource;

  @Mock
  private PlatformTransactionManager tenantTransactionManager;

  @InjectMocks
  private ComponentServiceImpl componentService;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private Component component1;
  private Component component2;
  private List<Object[]> mockResults;

  @BeforeEach
  void setUp() {
    component1 = new Component();
    component1.setId(1L);
    component1.setUuid("uuid-1");
    component1.setUid("cmsitem_1");
    component1.setComponentTypeId(10L);
    component1.setName("Main Header");
    component1.setStatus(ComponentStatus.PUBLISHED);
    component1.setIsVisible(true);
    component1.setCreatedAt(LocalDateTime.now());
    component1.setUpdatedAt(LocalDateTime.now());

    component2 = new Component();
    component2.setId(2L);
    component2.setUuid("uuid-2");
    component2.setUid("cmsitem_2");
    component2.setComponentTypeId(11L);
    component2.setName("Main Footer");
    component2.setStatus(ComponentStatus.DRAFT);
    component2.setIsVisible(false);
    component2.setCreatedAt(LocalDateTime.now());
    component2.setUpdatedAt(LocalDateTime.now());

    mockResults = new ArrayList<>();
    mockResults.add(new Object[] { component1, "NavigationComponent", 5L });
    mockResults.add(new Object[] { component2, "SimpleBannerComponent", 3L });
  }

  @Test
  void getAllComponentsWithTypeNames_ShouldReturnFlatDTOList() {
    when(componentRepository.findAllWithTypeNamesAndEntryCount()).thenReturn(mockResults);

    List<ComponentListItemResponse> result = componentService.getAllComponentsWithTypeNames();

    assertThat(result).hasSize(2);
    verify(componentRepository).findAllWithTypeNamesAndEntryCount();
  }

  @Test
  void getAllComponentsWithTypeNames_ShouldMapComponentTypeName() {
    when(componentRepository.findAllWithTypeNamesAndEntryCount()).thenReturn(mockResults);

    List<ComponentListItemResponse> result = componentService.getAllComponentsWithTypeNames();

    ComponentListItemResponse first = result.get(0);
    assertThat(first.componentTypeName()).isEqualTo("NavigationComponent");
    assertThat(first.id()).isEqualTo(1L);
    assertThat(first.componentTypeId()).isEqualTo(10L);
    assertThat(first.isVisible()).isTrue();

    ComponentListItemResponse second = result.get(1);
    assertThat(second.componentTypeName()).isEqualTo("SimpleBannerComponent");
    assertThat(second.id()).isEqualTo(2L);
    assertThat(second.isVisible()).isFalse();
  }

  @Test
  void getAllComponentsWithTypeNames_ShouldHandleNullTypeName() {
    List<Object[]> resultsWithNull = new ArrayList<>();
    resultsWithNull.add(new Object[] { component1, null, 0L });
    when(componentRepository.findAllWithTypeNamesAndEntryCount()).thenReturn(resultsWithNull);

    List<ComponentListItemResponse> result = componentService.getAllComponentsWithTypeNames();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).componentTypeName()).isNull();
  }

  @Test
  void getAllComponentsWithTypeNames_ShouldReturnEmptyListWhenNoComponents() {
    when(componentRepository.findAllWithTypeNamesAndEntryCount()).thenReturn(List.of());

    List<ComponentListItemResponse> result = componentService.getAllComponentsWithTypeNames();

    assertThat(result).isEmpty();
    verify(componentRepository).findAllWithTypeNamesAndEntryCount();
  }

  @Test
  void getAllComponentsWithTypeNames_ShouldMaintainOrderFromRepository() {
    when(componentRepository.findAllWithTypeNamesAndEntryCount()).thenReturn(mockResults);

    List<ComponentListItemResponse> result = componentService.getAllComponentsWithTypeNames();

    assertThat(result.get(0).id()).isEqualTo(1L);
    assertThat(result.get(1).id()).isEqualTo(2L);
  }

  @Test
  void updateComposite_ShouldClearTitleWhenExplicitlyProvidedAsEmpty() throws Exception {
    Component component = buildComponent(10L);
    ComponentType componentType = buildComponentType();
    ComponentI18n existingTranslation = buildTranslation(component.getId(), "Existing title", "Existing subtitle",
        "Existing description");

    when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));
    when(componentTypeRepository.findById(component.getComponentTypeId())).thenReturn(Optional.of(componentType));
    when(componentRepository.save(component)).thenReturn(component);
    when(componentI18nRepository.findByComponentIdAndLanguage(component.getId(), Language.EN))
        .thenReturn(Optional.of(existingTranslation));
    when(componentI18nRepository.save(any(ComponentI18n.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(securityHelper.getCurrentUserIdOrNull()).thenReturn(99L);

    UpdateComponentCompositeRequest request = new UpdateComponentCompositeRequest(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        Map.of(Language.EN, updateCommand("{\"title\":\"   \"}")));

    componentService.updateComposite(component.getId(), request);

    assertThat(existingTranslation.getTitle()).isEmpty();
    assertThat(existingTranslation.getSubtitle()).isEqualTo("Existing subtitle");
    assertThat(existingTranslation.getDescription()).isEqualTo("Existing description");
  }

  @Test
  void updateComposite_ShouldPreserveTitleWhenOnlySubtitleIsProvided() throws Exception {
    Component component = buildComponent(11L);
    ComponentType componentType = buildComponentType();
    ComponentI18n existingTranslation = buildTranslation(component.getId(), "Existing title", "Existing subtitle",
        "Existing description");

    when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));
    when(componentTypeRepository.findById(component.getComponentTypeId())).thenReturn(Optional.of(componentType));
    when(componentRepository.save(component)).thenReturn(component);
    when(componentI18nRepository.findByComponentIdAndLanguage(component.getId(), Language.EN))
        .thenReturn(Optional.of(existingTranslation));
    when(componentI18nRepository.save(any(ComponentI18n.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(securityHelper.getCurrentUserIdOrNull()).thenReturn(99L);

    UpdateComponentCompositeRequest request = new UpdateComponentCompositeRequest(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        Map.of(Language.EN, updateCommand("{\"subtitle\":\" Updated subtitle \"}")));

    componentService.updateComposite(component.getId(), request);

    assertThat(existingTranslation.getTitle()).isEqualTo("Existing title");
    assertThat(existingTranslation.getSubtitle()).isEqualTo("Updated subtitle");
    assertThat(existingTranslation.getDescription()).isEqualTo("Existing description");
  }

  @Test
  void updateComposite_ShouldNotOverwriteFieldsThatAreMissingFromPayload() throws Exception {
    Component component = buildComponent(12L);
    ComponentType componentType = buildComponentType();
    ComponentI18n existingTranslation = buildTranslation(component.getId(), "Existing title", "Existing subtitle",
        "Existing description");

    when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));
    when(componentTypeRepository.findById(component.getComponentTypeId())).thenReturn(Optional.of(componentType));
    when(componentRepository.save(component)).thenReturn(component);
    when(componentI18nRepository.findByComponentIdAndLanguage(component.getId(), Language.EN))
        .thenReturn(Optional.of(existingTranslation));
    when(componentI18nRepository.save(any(ComponentI18n.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(securityHelper.getCurrentUserIdOrNull()).thenReturn(99L);

    UpdateComponentCompositeRequest request = new UpdateComponentCompositeRequest(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        Map.of(Language.EN, updateCommand("{}")));

    componentService.updateComposite(component.getId(), request);

    assertThat(existingTranslation.getTitle()).isEqualTo("Existing title");
    assertThat(existingTranslation.getSubtitle()).isEqualTo("Existing subtitle");
    assertThat(existingTranslation.getDescription()).isEqualTo("Existing description");
  }

  private Component buildComponent(Long id) {
    Component component = new Component();
    component.setId(id);
    component.setUid("component_" + id);
    component.setName("Component " + id);
    component.setComponentTypeId(101L);
    component.setIsVisible(true);
    component.setStatus(ComponentStatus.DRAFT);
    component.setCreatedAt(LocalDateTime.now());
    component.setUpdatedAt(LocalDateTime.now());
    return component;
  }

  private ComponentType buildComponentType() {
    ComponentType componentType = new ComponentType();
    componentType.setId(101L);
    componentType.setUid("hero-banner");
    componentType.setName("Hero Banner");
    componentType.setNavigationAware(false);
    return componentType;
  }

  private ComponentI18n buildTranslation(Long componentId, String title, String subtitle, String description) {
    ComponentI18n translation = new ComponentI18n();
    translation.setComponentId(componentId);
    translation.setLanguage(Language.EN);
    translation.setStatus(ComponentStatus.DRAFT);
    translation.setTitle(title);
    translation.setSubtitle(subtitle);
    translation.setDescription(description);
    return translation;
  }

  private ComponentI18nUpdateCommand updateCommand(String json) throws Exception {
    return new ComponentI18nUpdateCommand(objectMapper.readTree(json));
  }
}
