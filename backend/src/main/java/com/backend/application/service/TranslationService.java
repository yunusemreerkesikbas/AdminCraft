package com.backend.application.service;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentTranslation;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.ComponentRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TranslationService {

  void upsertTranslations(Component component,
      Map<Language, ComponentRequest.I18nPayload> translations);

  Map<Long, ComponentTranslation> findByComponentIdsAndLanguage(List<Long> componentIds,
      Language language);

  Map<Language, ComponentTranslation> findByComponentIdAndLanguages(Long componentId,
      Set<Language> languages);

  ComponentTranslation getForLanguageWithFallback(Long componentId,
      Long tenantId,
      Language requested);

  void deleteByComponentId(Long componentId);
}
