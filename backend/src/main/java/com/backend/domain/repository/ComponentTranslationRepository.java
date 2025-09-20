package com.backend.domain.repository;

import com.backend.domain.entity.ComponentTranslation;
import com.backend.domain.enums.Language;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ComponentTranslationRepository {
  List<ComponentTranslation> findAllByComponentId(Long componentId);

  Optional<ComponentTranslation> findByComponentIdAndLanguage(Long componentId, Language language);

  List<ComponentTranslation> findAllByComponentIdInAndLanguage(List<Long> componentIds, Language language);

  List<ComponentTranslation> findAllByComponentIdInAndLanguageIn(List<Long> componentIds, List<Language> languages);

  ComponentTranslation save(ComponentTranslation translation);

  void deleteByComponentId(Long componentId);
}
