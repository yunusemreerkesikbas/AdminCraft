package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.CategoryI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.CategoryI18nRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CategoryI18nRepositoryImpl implements CategoryI18nRepository {

    private final CategoryI18nJpaRepository jpaRepository;

    @Override
    public Optional<CategoryI18n> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<CategoryI18n> findByCategoryIdAndLanguage(Long categoryId, Language language) {
        return jpaRepository.findByCategoryIdAndLanguage(categoryId, language);
    }

    @Override
    public List<CategoryI18n> findByCategoryId(Long categoryId) {
        return jpaRepository.findByCategoryId(categoryId);
    }

    @Override
    @Transactional
    public CategoryI18n save(CategoryI18n entity) {
        return jpaRepository.save(entity);
    }

    @Override
    @Transactional
    public List<CategoryI18n> saveAll(List<CategoryI18n> entities) {
        return jpaRepository.saveAll(entities);
    }

    @Override
    @Transactional
    public void delete(CategoryI18n entity) {
        jpaRepository.delete(entity);
    }

    @Override
    @Transactional
    public void deleteByCategoryId(Long categoryId) {
        jpaRepository.deleteByCategoryId(categoryId);
    }

    @Override
    public boolean existsByCategoryIdAndLanguage(Long categoryId, Language language) {
        return jpaRepository.existsByCategoryIdAndLanguage(categoryId, language);
    }
}
