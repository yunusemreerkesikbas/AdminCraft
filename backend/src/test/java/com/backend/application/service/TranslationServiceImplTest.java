package com.backend.application.service;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentTranslation;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentTranslationRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.presentation.dto.request.ComponentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TranslationServiceImplTest {

  private ComponentTranslationRepository translationRepository;
  private TenantRepository tenantRepository;
  private TranslationServiceImpl service;

  @BeforeEach
  void setup() {
    translationRepository = mock(ComponentTranslationRepository.class);
    tenantRepository = mock(TenantRepository.class);
    service = new TranslationServiceImpl(translationRepository, tenantRepository);
  }

  @Test
  void upsertTranslations_createsOrUpdates() {
    Component c = new Component();
    c.setId(10L);
    var payload = new ComponentRequest.I18nPayload("t", "s", "d");

    when(translationRepository.findByComponentIdAndLanguage(10L, Language.TR))
        .thenReturn(Optional.empty());

    service.upsertTranslations(c, Map.of(Language.TR, payload));

    verify(translationRepository, times(1)).save(any(ComponentTranslation.class));
  }

  @Test
  void findByComponentIdsAndLanguage_returnsMap() {
    ComponentTranslation t = new ComponentTranslation();
    Component c = new Component();
    c.setId(1L);
    t.setComponent(c);
    t.setLanguage(Language.TR);
    when(translationRepository.findAllByComponentIdInAndLanguage(List.of(1L), Language.TR))
        .thenReturn(List.of(t));

    var map = service.findByComponentIdsAndLanguage(List.of(1L), Language.TR);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(1L));
  }
}
