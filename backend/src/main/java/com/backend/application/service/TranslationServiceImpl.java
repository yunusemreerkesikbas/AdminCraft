package com.backend.application.service;

import com.backend.domain.entity.Component;
import com.backend.domain.entity.ComponentTranslation;
import com.backend.domain.enums.Language;
import com.backend.domain.exception.TenantNotFoundException;
import com.backend.domain.repository.ComponentTranslationRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.presentation.dto.request.ComponentRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TranslationServiceImpl implements TranslationService {

  private final ComponentTranslationRepository translationRepository;
  private final TenantRepository tenantRepository;

  public TranslationServiceImpl(ComponentTranslationRepository translationRepository,
      TenantRepository tenantRepository) {
    this.translationRepository = translationRepository;
    this.tenantRepository = tenantRepository;
  }

  @Override
  public void upsertTranslations(Component component,
      Map<Language, ComponentRequest.I18nPayload> translations) {
    if (translations == null || translations.isEmpty()) {
      return;
    }
    Long componentId = component.getId();
    for (Map.Entry<Language, ComponentRequest.I18nPayload> e : translations.entrySet()) {
      Language lang = e.getKey();
      ComponentRequest.I18nPayload payload = e.getValue();
      ComponentTranslation t = translationRepository
          .findByComponentIdAndLanguage(componentId, lang)
          .orElseGet(() -> {
            ComponentTranslation nt = new ComponentTranslation();
            nt.setComponent(component);
            nt.setLanguage(lang);
            return nt;
          });
      if (payload != null) {
        t.setTitle(payload.title());
        t.setSubtitle(payload.subtitle());
        t.setData(payload.data());
      }
      translationRepository.save(t);
    }
  }

  @Override
  public Map<Long, ComponentTranslation> findByComponentIdsAndLanguage(List<Long> componentIds,
      Language language) {
    if (componentIds == null || componentIds.isEmpty()) {
      return Map.of();
    }
    List<ComponentTranslation> list = translationRepository
        .findAllByComponentIdInAndLanguage(componentIds, language);
    return list.stream().collect(Collectors.toMap(t -> t.getComponent().getId(), t -> t));
  }

  @Override
  public Map<Language, ComponentTranslation> findByComponentIdAndLanguages(Long componentId,
      Set<Language> languages) {
    Map<Language, ComponentTranslation> map = new HashMap<>();
    if (languages == null || languages.isEmpty()) {
      return map;
    }
    for (Language lang : languages) {
      translationRepository.findByComponentIdAndLanguage(componentId, lang)
          .ifPresent(t -> map.put(lang, t));
    }
    return map;
  }

  @Override
  public ComponentTranslation getForLanguageWithFallback(Long componentId,
      Long tenantId,
      Language requested) {
    var tenant = tenantRepository.findById(tenantId)
        .orElseThrow(() -> new TenantNotFoundException(tenantId));
    Language effective = tenant.getSupportedLanguages().contains(requested)
        ? requested
        : tenant.getDefaultLanguage();
    return translationRepository.findByComponentIdAndLanguage(componentId, effective)
        .orElse(null);
  }

  @Override
  public void deleteByComponentId(Long componentId) {
    translationRepository.deleteByComponentId(componentId);
  }
}
