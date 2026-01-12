package com.backend.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.CategoryI18nDto;
import com.backend.domain.entity.Category;
import com.backend.domain.entity.CategoryI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.CategoryI18nRepository;
import com.backend.domain.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryI18nRepository categoryI18nRepository;

    @Override
    @Transactional
    public Category createComposite(String code, Long parentId, Integer sortOrder, Boolean isVisible,
            Map<Language, CategoryI18nDto> translations, Long createdBy) {
        log.debug("Creating category with code: {}", code);

        if (categoryRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Category code already exists: " + code);
        }

        if (parentId != null && categoryRepository.findById(parentId).isEmpty()) {
            throw new IllegalArgumentException("Parent category not found: " + parentId);
        }

        int nextSortOrder = sortOrder != null ? sortOrder
                : categoryRepository.findMaxSortOrderByParentId(parentId) + 1;

        Category category = new Category();
        category.setCode(code);
        category.setParentId(parentId);
        category.setSortOrder(nextSortOrder);
        category.setIsVisible(isVisible != null ? isVisible : true);
        category.setCreatedBy(createdBy);
        category.setUpdatedBy(createdBy);

        Category saved = categoryRepository.save(category);

        for (Map.Entry<Language, CategoryI18nDto> entry : translations.entrySet()) {
            CategoryI18n i18n = new CategoryI18n();
            i18n.setCategory(saved);
            i18n.setLanguage(entry.getKey());
            i18n.setName(entry.getValue().name());
            i18n.setDescription(entry.getValue().description());
            i18n.setCreatedBy(createdBy);
            i18n.setUpdatedBy(createdBy);
            categoryI18nRepository.save(i18n);
        }

        log.info("Created category id: {}, code: {}", saved.getId(), saved.getCode());
        return categoryRepository.findByIdWithI18n(saved.getId()).orElse(saved);
    }

    @Override
    @Transactional
    public Category updateComposite(Long id, Long parentId, Integer sortOrder, Boolean isVisible,
            Map<Language, CategoryI18nDto> translations, Long updatedBy) {
        log.debug("Updating category id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));

        if (parentId != null) {
            if (parentId.equals(id)) {
                throw new IllegalArgumentException("Category cannot be its own parent");
            }
            if (categoryRepository.findById(parentId).isEmpty()) {
                throw new IllegalArgumentException("Parent category not found: " + parentId);
            }
        }

        category.setParentId(parentId);
        if (sortOrder != null)
            category.setSortOrder(sortOrder);
        if (isVisible != null)
            category.setIsVisible(isVisible);
        category.setUpdatedBy(updatedBy);

        Category saved = categoryRepository.save(category);

        for (Map.Entry<Language, CategoryI18nDto> entry : translations.entrySet()) {
            CategoryI18n i18n = categoryI18nRepository.findByCategoryIdAndLanguage(id, entry.getKey())
                    .orElseGet(() -> {
                        CategoryI18n newI18n = new CategoryI18n();
                        newI18n.setCategory(saved);
                        newI18n.setLanguage(entry.getKey());
                        newI18n.setCreatedBy(updatedBy);
                        return newI18n;
                    });
            i18n.setName(entry.getValue().name());
            i18n.setDescription(entry.getValue().description());
            i18n.setUpdatedBy(updatedBy);
            categoryI18nRepository.save(i18n);
        }

        log.info("Updated category id: {}, code: {}", saved.getId(), saved.getCode());
        return categoryRepository.findByIdWithI18n(saved.getId()).orElse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting category id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));

        if (categoryRepository.hasChildren(id)) {
            throw new IllegalStateException("Cannot delete category with children. Remove children first.");
        }

        if (categoryRepository.hasProducts(id)) {
            throw new IllegalStateException(
                    "Cannot delete category with products. Remove products from category first.");
        }

        categoryRepository.delete(category);
        log.info("Deleted category id: {}, code: {}", id, category.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findByIdWithI18n(Long id) {
        return categoryRepository.findByIdWithI18n(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findByUid(String uid) {
        return categoryRepository.findByUid(uid);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findByCode(String code) {
        return categoryRepository.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findRootCategories() {
        return categoryRepository.findRootCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getTree() {
        return categoryRepository.findRootCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findByParentId(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasChildren(Long categoryId) {
        return categoryRepository.hasChildren(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasProducts(Long categoryId) {
        return categoryRepository.hasProducts(categoryId);
    }
}
