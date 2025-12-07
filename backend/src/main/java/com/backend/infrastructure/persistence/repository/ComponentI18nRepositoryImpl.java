package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ComponentI18n;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentI18nRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ComponentI18nRepositoryImpl implements ComponentI18nRepository {

    private final ComponentI18nJpaRepository jpaRepository;

    @Override
    public Optional<ComponentI18n> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ComponentI18n> findByUuid(String uuid) {
        return jpaRepository.findByUuid(uuid);
    }

    @Override
    public Optional<ComponentI18n> findByUid(String uid) {
        return jpaRepository.findByUid(uid);
    }

    @Override
    public Optional<ComponentI18n> findByComponentIdAndLanguage(Long componentId, Language language) {
        return jpaRepository.findByComponentIdAndLanguage(componentId, language);
    }

    @Override
    public List<ComponentI18n> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<ComponentI18n> findByComponentId(Long componentId) {
        return jpaRepository.findByComponentId(componentId);
    }

    @Override
    public List<ComponentI18n> findByLanguage(Language language) {
        return jpaRepository.findByLanguage(language);
    }

    @Override
    public List<ComponentI18n> findByStatus(ComponentStatus status) {
        return jpaRepository.findByStatus(status);
    }

    @Override
    public List<ComponentI18n> findByLanguageAndStatus(Language language, ComponentStatus status) {
        return jpaRepository.findByLanguageAndStatus(language, status);
    }

    @Override
    public ComponentI18n save(ComponentI18n entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public void delete(ComponentI18n entity) {
        jpaRepository.delete(entity);
    }

    @Override
    public boolean existsByUid(String uid) {
        return jpaRepository.existsByUid(uid);
    }

    @Override
    public List<ComponentI18n> findByComponentIdInAndLanguage(List<Long> componentIds, Language language) {
        return jpaRepository.findByComponentIdInAndLanguage(componentIds, language);
    }
}

