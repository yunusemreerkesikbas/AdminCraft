package com.backend.domain.repository;

import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import java.util.List;
import java.util.Optional;

public interface ComponentI18nRepository {
    Optional<ComponentI18n> findById(Long id);

    Optional<ComponentI18n> findByUuid(String uuid);

    Optional<ComponentI18n> findByUid(String uid);

    Optional<ComponentI18n> findByComponentIdAndLanguage(Long componentId, Language language);

    List<ComponentI18n> findAll();

    List<ComponentI18n> findByComponentId(Long componentId);

    List<ComponentI18n> findByLanguage(Language language);

    List<ComponentI18n> findByStatus(ComponentStatus status);

    List<ComponentI18n> findByLanguageAndStatus(Language language, ComponentStatus status);

    ComponentI18n save(ComponentI18n entity);

    void delete(ComponentI18n entity);

    boolean existsByUid(String uid);

    List<ComponentI18n> findByComponentIdInAndLanguage(List<Long> componentIds, Language language);
}
