package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.application.dto.response.ComponentListItemResponse;
import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.repository.ComponentI18nRepository;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTypeRepository;

@ExtendWith(MockitoExtension.class)
class ComponentServiceImplTest {

  @Mock
  private ComponentRepository componentRepository;

  @Mock
  private ComponentI18nRepository componentI18nRepository;

  @Mock
  private ComponentTypeService componentTypeService;

  @Mock
  private ComponentTypeRepository componentTypeRepository;

  @InjectMocks
  private ComponentServiceImpl componentService;

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
    mockResults.add(new Object[] { component1, "Navigation", 5L });
    mockResults.add(new Object[] { component2, "Footer", 3L });
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
    assertThat(first.componentTypeName()).isEqualTo("Navigation");
    assertThat(first.id()).isEqualTo(1L);
    assertThat(first.componentTypeId()).isEqualTo(10L);
    assertThat(first.isVisible()).isTrue();

    ComponentListItemResponse second = result.get(1);
    assertThat(second.componentTypeName()).isEqualTo("Footer");
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
}
