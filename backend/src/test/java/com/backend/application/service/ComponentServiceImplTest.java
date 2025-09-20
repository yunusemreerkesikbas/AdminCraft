package com.backend.application.service;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentTranslation;
import com.backend.domain.enums.ComponentType;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.ComponentTranslationRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.presentation.dto.request.ComponentListFilter;
import com.backend.presentation.dto.request.ComponentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.backend.shared.common.SecurityUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComponentServiceImplTest {

  private ComponentRepository componentRepository;
  private ComponentTranslationRepository translationRepository;
  private TenantRepository tenantRepository;
  private LanguageService languageService;
  private TranslationService translationService;
  private ComponentServiceImpl service;

  @BeforeEach
  void setup() {
    componentRepository = mock(ComponentRepository.class);
    translationRepository = mock(ComponentTranslationRepository.class);
    tenantRepository = mock(TenantRepository.class);
    languageService = mock(LanguageService.class);
    translationService = mock(TranslationService.class);
    service = new ComponentServiceImpl(componentRepository, translationRepository,
        tenantRepository, languageService, translationService);
  }

  @Test
  void create_validationsDelegatedToLanguageService_andTranslationsViaTranslationService() {
    Long tenantId = 5L;
    ComponentRequest req = new ComponentRequest(
        tenantId,
        ComponentType.NAVBAR,
        "header",
        null,
        true,
        0,
        Map.of("tr", new ComponentRequest.I18nPayload("T", null, null)));

    when(componentRepository.findByTenantAndTypeAndKey(tenantId, req.type(), req.key()))
        .thenReturn(Optional.empty());

    Component saved = new Component();
    saved.setId(100L);
    when(componentRepository.save(any(Component.class))).thenReturn(saved);
    when(translationRepository.findByComponentIdAndLanguage(100L, Language.TR))
        .thenReturn(java.util.Optional.empty());
    when(translationRepository.findByComponentIdAndLanguage(100L, Language.EN))
        .thenReturn(java.util.Optional.empty());
    try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
      mocked.when(SecurityUtil::getCurrentUserIdOrThrow).thenReturn(1L);
      service.create(tenantId, req);
    }

    verify(languageService, times(1)).validateTranslationKeys(eq(tenantId), any());
    verify(translationService, times(1)).upsertTranslations(eq(saved), any());
  }

  @Test
  void getSiteComponents_usesEffectiveLanguageAndBatchRead() {
    Long tenantId = 5L;
    when(languageService.resolveEffectiveLanguage(tenantId, Language.EN))
        .thenReturn(Language.EN);

    Component c = new Component();
    c.setId(1L);
    when(componentRepository.findActiveVisibleByTenantIdAndType(tenantId, ComponentType.NAVBAR))
        .thenReturn(List.of(c));

    when(translationService.findByComponentIdsAndLanguage(List.of(1L), Language.EN))
        .thenReturn(Map.of(1L, new ComponentTranslation()));

    var list = service.getSiteComponents(tenantId, ComponentType.NAVBAR, Language.EN);
    assertEquals(1, list.size());
  }
}
