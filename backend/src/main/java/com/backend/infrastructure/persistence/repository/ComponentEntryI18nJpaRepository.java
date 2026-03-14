package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface ComponentEntryI18nJpaRepository extends JpaRepository<ComponentEntryI18n, Long> {

  @Query("SELECT ei FROM ComponentEntryI18n ei WHERE ei.entryId IN :entryIds")
  List<ComponentEntryI18n> findByEntryIdIn(@Param("entryIds") List<Long> entryIds);

  List<ComponentEntryI18n> findByEntryIdInAndLanguage(List<Long> entryIds, Language language);

  List<ComponentEntryI18n> findByEntryId(Long entryId);

  Optional<ComponentEntryI18n> findByEntryIdAndLanguage(Long entryId, Language language);

  boolean existsByUid(String uid);

  void deleteByEntryId(Long entryId);
}
