package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.enums.Language;

public interface ComponentEntryI18nRepository {
    ComponentEntryI18n save(ComponentEntryI18n entryI18n);

    Optional<ComponentEntryI18n> findById(Long id);

    Optional<ComponentEntryI18n> findByEntryIdAndLanguage(Long entryId, Language language);

    List<ComponentEntryI18n> findByEntryId(Long entryId);

    List<ComponentEntryI18n> findByEntryIdIn(List<Long> entryIds);

    void delete(ComponentEntryI18n entryI18n);

    boolean existsByUid(String uid);

    List<ComponentEntryI18n> findByEntryIdInAndLanguage(List<Long> entryIds, Language language);

    List<ComponentEntryI18n> saveAll(List<ComponentEntryI18n> i18nList);
}
