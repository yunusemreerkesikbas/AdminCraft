package com.backend.infrastructure.persistence.tenant.repository;

import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JpaComponentEntryI18nRepository extends JpaRepository<ComponentEntryI18n, Long> {
    Optional<ComponentEntryI18n> findByEntryIdAndLanguage(Long entryId, Language language);
    List<ComponentEntryI18n> findByEntryId(Long entryId);
    List<ComponentEntryI18n> findByEntryIdIn(List<Long> entryIds);
    boolean existsByUid(String uid);
    List<ComponentEntryI18n> findByEntryIdInAndLanguage(List<Long> entryIds, Language language);
}
