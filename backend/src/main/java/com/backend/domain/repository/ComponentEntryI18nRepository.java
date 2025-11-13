package com.backend.domain.repository;

import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.enums.Language;
import java.util.List;
import java.util.Optional;

public interface ComponentEntryI18nRepository {
    ComponentEntryI18n save(ComponentEntryI18n entryI18n);
    Optional<ComponentEntryI18n> findById(Long id);
    Optional<ComponentEntryI18n> findByEntryIdAndLanguage(Long entryId, Language language);
    List<ComponentEntryI18n> findByEntryId(Long entryId);
    void delete(ComponentEntryI18n entryI18n);
    boolean existsByUid(String uid);
}
