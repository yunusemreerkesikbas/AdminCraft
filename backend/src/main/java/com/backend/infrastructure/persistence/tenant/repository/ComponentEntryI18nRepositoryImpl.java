package com.backend.infrastructure.persistence.tenant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.domain.entity.ComponentEntryI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ComponentEntryI18nRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ComponentEntryI18nRepositoryImpl implements ComponentEntryI18nRepository {

    private final JpaComponentEntryI18nRepository jpaRepository;

    @Override
    public ComponentEntryI18n save(ComponentEntryI18n entryI18n) {
        return jpaRepository.save(entryI18n);
    }

    @Override
    public Optional<ComponentEntryI18n> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ComponentEntryI18n> findByEntryIdAndLanguage(Long entryId, Language language) {
        return jpaRepository.findByEntryIdAndLanguage(entryId, language);
    }

    @Override
    public List<ComponentEntryI18n> findByEntryId(Long entryId) {
        return jpaRepository.findByEntryId(entryId);
    }

    @Override
    public void delete(ComponentEntryI18n entryI18n) {
        jpaRepository.delete(entryI18n);
    }

    @Override
    public boolean existsByUid(String uid) {
        return jpaRepository.existsByUid(uid);
    }

    @Override
    public List<ComponentEntryI18n> findByEntryIdInAndLanguage(List<Long> entryIds, Language language) {
        return jpaRepository.findByEntryIdInAndLanguage(entryIds, language);
    }
}
