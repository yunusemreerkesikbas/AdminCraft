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
import com.backend.infrastructure.tenant.TenantContext;
import com.backend.shared.common.HtmlSanitizer;

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
        TenantContext.validateActive();
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

        List<CategoryI18n> i18nList = translations.entrySet().stream()
                .map(entry -> {
                    CategoryI18n i18n = new CategoryI18n();
                    i18n.setCategory(saved);
                    i18n.setLanguage(entry.getKey());
                    i18n.setName(entry.getValue().name());
                    i18n.setDescription(HtmlSanitizer.sanitizeRichText(entry.getValue().description()));
                    i18n.setCreatedBy(createdBy);
                    i18n.setUpdatedBy(createdBy);
                    return i18n;
                })
                .toList();
        categoryI18nRepository.saveAll(i18nList);

        log.info("Created category id: {}, code: {}", saved.getId(), saved.getCode());
        return categoryRepository.findByIdWithI18n(saved.getId()).orElse(saved);
    }

    @Override
    @Transactional
    public Category updateComposite(Long id, Long parentId, Integer sortOrder, Boolean isVisible,
            Map<Language, CategoryI18nDto> translations, Long updatedBy) {
        TenantContext.validateActive();
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

        List<CategoryI18n> i18nList = translations.entrySet().stream()
                .map(entry -> {
                    CategoryI18n i18n = categoryI18nRepository.findByCategoryIdAndLanguage(id, entry.getKey())
                            .orElseGet(() -> {
                                CategoryI18n newI18n = new CategoryI18n();
                                newI18n.setCategory(saved);
                                newI18n.setLanguage(entry.getKey());
                                newI18n.setCreatedBy(updatedBy);
                                return newI18n;
                            });
                    i18n.setName(entry.getValue().name());
                    i18n.setDescription(HtmlSanitizer.sanitizeRichText(entry.getValue().description()));
                    i18n.setUpdatedBy(updatedBy);
                    return i18n;
                })
                .toList();
        categoryI18nRepository.saveAll(i18nList);

        log.info("Updated category id: {}, code: {}", saved.getId(), saved.getCode());
        return categoryRepository.findByIdWithI18n(saved.getId()).orElse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        TenantContext.validateActive();
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
        TenantContext.validateActive();
        return categoryRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findByIdWithI18n(Long id) {
        TenantContext.validateActive();
        return categoryRepository.findByIdWithI18n(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findByUid(String uid) {
        TenantContext.validateActive();
        return categoryRepository.findByUid(uid);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findByCode(String code) {
        TenantContext.validateActive();
        return categoryRepository.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        TenantContext.validateActive();
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findRootCategories() {
        TenantContext.validateActive();
        return categoryRepository.findRootCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getTree() {
        TenantContext.validateActive();
        return categoryRepository.findRootCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findByParentId(Long parentId) {
        TenantContext.validateActive();
        return categoryRepository.findByParentId(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasChildren(Long categoryId) {
        TenantContext.validateActive();
        return categoryRepository.hasChildren(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasProducts(Long categoryId) {
        TenantContext.validateActive();
        return categoryRepository.hasProducts(categoryId);
    }
}
