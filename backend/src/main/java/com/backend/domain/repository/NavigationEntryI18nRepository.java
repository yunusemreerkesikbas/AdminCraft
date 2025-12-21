package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.NavigationEntryI18n;
import com.backend.domain.enums.Language;

@Repository
public interface NavigationEntryI18nRepository extends JpaRepository<NavigationEntryI18n, Long> {

  Optional<NavigationEntryI18n> findByEntryIdAndLanguage(Long entryId, Language language);

  List<NavigationEntryI18n> findByEntryId(Long entryId);

  List<NavigationEntryI18n> findByEntryIdIn(List<Long> entryIds);

  @Modifying
  void deleteByEntryId(Long entryId);

  boolean existsByEntryIdAndLanguage(Long entryId, Language language);
}
