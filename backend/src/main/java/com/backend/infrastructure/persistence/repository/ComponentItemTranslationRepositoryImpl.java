package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ComponentItemTranslation;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentItemTranslationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ComponentItemTranslationRepositoryImpl implements ComponentItemTranslationRepository {

  private final ComponentItemTranslationJpaRepository jpaRepository;

  public ComponentItemTranslationRepositoryImpl(ComponentItemTranslationJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public List<ComponentItemTranslation> findAllByItemIdInAndLanguageIn(List<Long> itemIds, List<Language> languages) {
    if (itemIds == null || itemIds.isEmpty() || languages == null || languages.isEmpty())
      return List.of();
    return jpaRepository.findAllByItemIdInAndLanguageIn(itemIds, languages);
  }

  @Override
  public Optional<ComponentItemTranslation> findByItemIdAndLanguage(Long itemId, Language language) {
    // Not implemented in JPA repository yet
    return Optional.empty();
  }

  @Override
  public ComponentItemTranslation save(ComponentItemTranslation translation) {
    return jpaRepository.save(translation);
  }

  @Override
  public void deleteByItemId(Long itemId) {
    // TODO: Add delete custom query if needed
  }
}

