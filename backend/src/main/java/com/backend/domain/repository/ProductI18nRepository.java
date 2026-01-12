package com.backend.domain.repository;

import com.backend.domain.entity.ProductI18n;
import com.backend.domain.enums.Language;

import java.util.List;
import java.util.Optional;

public interface ProductI18nRepository {

    Optional<ProductI18n> findById(Long id);

    Optional<ProductI18n> findByProductIdAndLanguage(Long productId, Language language);

    List<ProductI18n> findByProductId(Long productId);

    ProductI18n save(ProductI18n entity);

    List<ProductI18n> saveAll(List<ProductI18n> entities);

    void delete(ProductI18n entity);

    void deleteByProductId(Long productId);

    boolean existsByProductIdAndLanguage(Long productId, Language language);
}
