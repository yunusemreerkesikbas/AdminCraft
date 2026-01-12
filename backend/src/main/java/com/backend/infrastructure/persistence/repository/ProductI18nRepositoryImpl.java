package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.ProductI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.ProductI18nRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductI18nRepositoryImpl implements ProductI18nRepository {

    private final ProductI18nJpaRepository jpaRepository;

    @Override
    public Optional<ProductI18n> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ProductI18n> findByProductIdAndLanguage(Long productId, Language language) {
        return jpaRepository.findByProductIdAndLanguage(productId, language);
    }

    @Override
    public List<ProductI18n> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    @Transactional
    public ProductI18n save(ProductI18n entity) {
        return jpaRepository.save(entity);
    }

    @Override
    @Transactional
    public List<ProductI18n> saveAll(List<ProductI18n> entities) {
        return jpaRepository.saveAll(entities);
    }

    @Override
    @Transactional
    public void delete(ProductI18n entity) {
        jpaRepository.delete(entity);
    }

    @Override
    @Transactional
    public void deleteByProductId(Long productId) {
        jpaRepository.deleteByProductId(productId);
    }

    @Override
    public boolean existsByProductIdAndLanguage(Long productId, Language language) {
        return jpaRepository.existsByProductIdAndLanguage(productId, language);
    }
}
