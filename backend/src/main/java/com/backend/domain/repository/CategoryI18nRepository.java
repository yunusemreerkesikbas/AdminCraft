package com.backend.domain.repository;

import com.backend.domain.entity.CategoryI18n;
import com.backend.domain.enums.Language;

import java.util.List;
import java.util.Optional;

public interface CategoryI18nRepository {

    Optional<CategoryI18n> findById(Long id);

    Optional<CategoryI18n> findByCategoryIdAndLanguage(Long categoryId, Language language);

    List<CategoryI18n> findByCategoryId(Long categoryId);

    CategoryI18n save(CategoryI18n entity);

    List<CategoryI18n> saveAll(List<CategoryI18n> entities);

    void delete(CategoryI18n entity);

    void deleteByCategoryId(Long categoryId);

    boolean existsByCategoryIdAndLanguage(Long categoryId, Language language);
}
