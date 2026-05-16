package com.backend.application.cms.preview;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.domain.repository.CmsDraftOverrideRepository;
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
  private PageSlotRepository pageSlotRepository;
  @Mock
  private SlotComponentRepository slotComponentRepository;
  @Mock
  private ResponsiveMediaSetRepository responsiveMediaSetRepository;
  @Mock
  private ComponentTypeRepository componentTypeRepository;
  @Mock
  private NavigationNodeRepository navigationNodeRepository;

  private CmsDraftOverrideService service;

  @BeforeEach
  void setUp() {
    service = new CmsDraftOverrideService(
        draftOverrideRepository,
        componentRepository,
        componentI18nRepository,
        pageSlotRepository,
        slotComponentRepository,
        responsiveMediaSetRepository,
        componentTypeRepository,
        navigationNodeRepository,
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
}
