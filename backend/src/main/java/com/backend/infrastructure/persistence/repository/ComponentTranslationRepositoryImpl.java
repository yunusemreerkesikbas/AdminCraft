package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ComponentTranslation;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentTranslationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ComponentTranslationRepositoryImpl implements ComponentTranslationRepository {

  private final ComponentTranslationJpaRepository jpaRepository;

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
  public ComponentTranslation save(ComponentTranslation translation) {
    return jpaRepository.save(translation);
  }

  @Override
  public void deleteByComponentId(Long componentId) {
    List<ComponentTranslation> list = jpaRepository.findAllByComponentId(componentId);
    jpaRepository.deleteAll(list);
  }
}
