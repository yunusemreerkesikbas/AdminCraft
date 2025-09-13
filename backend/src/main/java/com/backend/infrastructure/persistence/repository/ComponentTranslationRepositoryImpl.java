package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ComponentTranslation;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentTranslationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ComponentTranslationRepositoryImpl implements ComponentTranslationRepository {

  @Autowired
  private ComponentTranslationJpaRepository jpaRepository;

  @Override
  public List<ComponentTranslation> findAllByComponentId(Long componentId) {
    return jpaRepository.findAllByComponentId(componentId);
  }

  @Override
  public Optional<ComponentTranslation> findByComponentIdAndLanguage(Long componentId, Language language) {
    return jpaRepository.findByComponentIdAndLanguage(componentId, language);
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
