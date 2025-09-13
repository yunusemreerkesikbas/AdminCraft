package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ComponentTranslation;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentTranslationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ComponentTranslationRepositoryImpl implements ComponentTranslationRepository {

  private final ComponentTranslationJpaRepository jpaRepository;

  // Constructor injection for better dependency management
  public ComponentTranslationRepositoryImpl(ComponentTranslationJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public List<ComponentTranslation> findAllByComponentId(Long componentId) {
    return jpaRepository.findAllByComponentId(componentId);
  }

  @Override
  public Optional<ComponentTranslation> findByComponentIdAndLanguage(Long componentId, Language language) {
    return jpaRepository.findByComponentIdAndLanguage(componentId, language);
  }

  @Override
  public List<ComponentTranslation> findAllByComponentIdInAndLanguage(List<Long> componentIds, Language language) {
    if (componentIds == null || componentIds.isEmpty()) {
      return List.of();
    }
    return jpaRepository.findAllByComponentIdInAndLanguage(componentIds, language);
  }

  @Override
  public List<ComponentTranslation> findAllByComponentIdIn(List<Long> componentIds) {
    if (componentIds == null || componentIds.isEmpty()) {
      return List.of();
    }
    return jpaRepository.findAllByComponentIdIn(componentIds);
  }

  @Override
  public Map<Long, Map<Language, ComponentTranslation>> findTranslationMapByComponentIds(List<Long> componentIds) {
    if (componentIds == null || componentIds.isEmpty()) {
      return Map.of();
    }

    List<ComponentTranslation> translations = jpaRepository.findAllByComponentIdIn(componentIds);

    // Group translations by componentId and then by language for efficient lookup
    return translations.stream()
        .collect(Collectors.groupingBy(
            ComponentTranslation::getComponentId,
            Collectors.toMap(
                ComponentTranslation::getLanguage,
                translation -> translation,
                (existing, replacement) -> replacement // Handle duplicates by keeping the last one
            )));
  }

  @Override
  public ComponentTranslation save(ComponentTranslation translation) {
    return jpaRepository.save(translation);
  }

  @Override
  public void deleteByComponentId(Long componentId) {
    List<ComponentTranslation> list = jpaRepository.findAllByComponentId(componentId);
    jpaRepository.deleteAll(list);
  }
}
