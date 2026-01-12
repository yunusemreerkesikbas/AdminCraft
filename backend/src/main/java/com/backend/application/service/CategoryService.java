package com.backend.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.backend.application.dto.request.CategoryI18nDto;
import com.backend.domain.entity.Category;
import com.backend.domain.enums.Language;

public interface CategoryService {

    Category createComposite(String code, Long parentId, Integer sortOrder, Boolean isVisible,
            Map<Language, CategoryI18nDto> translations, Long createdBy);

    Category updateComposite(Long id, Long parentId, Integer sortOrder, Boolean isVisible,
            Map<Language, CategoryI18nDto> translations, Long updatedBy);

    void delete(Long id);

    Optional<Category> findById(Long id);

    Optional<Category> findByIdWithI18n(Long id);

    Optional<Category> findByUid(String uid);

    Optional<Category> findByCode(String code);

    List<Category> findAll();

    List<Category> findRootCategories();

    List<Category> getTree();

    List<Category> findByParentId(Long parentId);

    boolean hasChildren(Long categoryId);

    boolean hasProducts(Long categoryId);
}
