package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ComponentItemTranslation;
import com.backend.domain.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComponentItemTranslationJpaRepository extends JpaRepository<ComponentItemTranslation, Long> {

  @Query("SELECT t FROM ComponentItemTranslation t WHERE t.item.id IN :itemIds AND t.language IN :languages")
  List<ComponentItemTranslation> findAllByItemIdInAndLanguageIn(
      @Param("itemIds") List<Long> itemIds,
      @Param("languages") List<Language> languages);
}

