package com.backend.domain.repository;

import com.backend.domain.entity.ComponentTranslation;
import com.backend.domain.enums.Language;

import java.util.List;
import java.util.Optional;

public interface ComponentTranslationRepository {
  List<ComponentTranslation> findAllByComponentId(Long componentId);

  Optional<ComponentTranslation> findByComponentIdAndLanguage(Long componentId, Language language);

  ComponentTranslation save(ComponentTranslation translation);

  void deleteByComponentId(Long componentId);
}
