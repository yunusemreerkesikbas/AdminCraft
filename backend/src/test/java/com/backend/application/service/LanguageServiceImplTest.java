package com.backend.application.service;

import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.TenantRepository;
import com.backend.presentation.dto.request.UpdateTenantLanguagesRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LanguageServiceImplTest {

  private TenantRepository tenantRepository;
  private LanguageServiceImpl service;

  @BeforeEach
  void setup() {
    tenantRepository = mock(TenantRepository.class);
    service = new LanguageServiceImpl(tenantRepository);
  }

  @Test
  void getPlatformLanguages_containsFive() {
    var list = service.getPlatformLanguages();
    assertTrue(list.size() >= 5);
    var codes = list.stream().map(i -> i.code()).toList();
    assertTrue(codes.containsAll(List.of("tr", "en", "es", "ar", "ru")));
  }

  @Test
  void getAndUpdateTenantLanguages_andValidation() {
    Long tenantId = 1L;
    Tenant tenant = new Tenant();
    tenant.setId(tenantId);
    tenant.setDefaultLanguage(Language.TR);
    tenant.setSupportedLanguages(Set.of(Language.TR, Language.EN));

    when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

    var resp = service.getTenantLanguages(tenantId);
    assertEquals("tr", resp.defaultLanguage());
    assertTrue(resp.supported().containsAll(List.of("tr", "en")));

    // update: default=en, supported=[tr]
    var request = new UpdateTenantLanguagesRequest("en", List.of("tr"));
    var updated = service.updateTenantLanguages(tenantId, request);
    assertEquals("en", updated.defaultLanguage());
    assertTrue(updated.supported().containsAll(List.of("tr", "en")));

    // validateTranslationKeys - ok for en
    service.validateTranslationKeys(tenantId, java.util.Map.of("en", new Object()));

    // validateTranslationKeys - fails for es
    var ex = assertThrows(IllegalArgumentException.class,
        () -> service.validateTranslationKeys(tenantId, java.util.Map.of("es", new Object())));
    assertTrue(ex.getMessage().contains("language.unsupported"));
  }

  @Test
  void resolveEffectiveLanguage_fallsBackToDefault() {
    Long tenantId = 2L;
    Tenant tenant = new Tenant();
    tenant.setId(tenantId);
    tenant.setDefaultLanguage(Language.TR);
    tenant.setSupportedLanguages(Set.of(Language.TR, Language.EN));
    when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

    var eff1 = service.resolveEffectiveLanguage(tenantId, Language.EN);
    assertEquals(Language.EN, eff1);

    var eff2 = service.resolveEffectiveLanguage(tenantId, Language.RU);
    assertEquals(Language.TR, eff2);
  }
}
