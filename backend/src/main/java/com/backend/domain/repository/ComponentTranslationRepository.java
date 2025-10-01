package com.backend.domain.repository;

import com.backend.domain.entity.ComponentTranslation;
import com.backend.domain.enums.Language;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ComponentTranslationRepository {
  List<ComponentTranslation> findAllByComponentId(Long componentId);

  Optional<ComponentTranslation> findByComponentIdAndLanguage(Long componentId, Language language);

  // Batch loading methods to fix N+1 query issue
  List<ComponentTranslation> findAllByComponentIdInAndLanguage(List<Long> componentIds, Language language);
  
  List<ComponentTranslation> findAllByComponentIdIn(List<Long> componentIds);
  
  // Optimized method for getting translations grouped by componentId and language
  Map<Long, Map<Language, ComponentTranslation>> findTranslationMapByComponentIds(List<Long> componentIds);

  ComponentTranslation save(ComponentTranslation translation);

  void deleteByComponentId(Long componentId);
}