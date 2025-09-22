package com.backend.domain.repository;

import com.backend.domain.entity.ComponentItemTranslation;
import com.backend.domain.enums.Language;

import java.util.List;
import java.util.Optional;

public interface ComponentItemTranslationRepository {

  List<ComponentItemTranslation> findAllByItemIdInAndLanguageIn(List<Long> itemIds, List<Language> languages);

  Optional<ComponentItemTranslation> findByItemIdAndLanguage(Long itemId, Language language);

  ComponentItemTranslation save(ComponentItemTranslation translation);

  void deleteByItemId(Long itemId);
}

