package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentI18nJpaRepository extends JpaRepository<ComponentI18n, Long> {
    Optional<ComponentI18n> findByUuid(String uuid);
    Optional<ComponentI18n> findByUid(String uid);
    Optional<ComponentI18n> findByComponentIdAndLanguage(Long componentId, Language language);
    List<ComponentI18n> findByComponentId(Long componentId);
    List<ComponentI18n> findByLanguage(Language language);
    List<ComponentI18n> findByStatus(ComponentStatus status);
    List<ComponentI18n> findByLanguageAndStatus(Language language, ComponentStatus status);
    boolean existsByUid(String uid);
}
