package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.domain.entity.Category;

public interface CategoryRepository {

    Optional<Category> findById(Long id);

    Optional<Category> findByIdWithI18n(Long id);

    Optional<Category> findByIdWithChildren(Long id);

    Optional<Category> findByUid(String uid);

    Optional<Category> findByCode(String code);

    List<Category> findAll();

    List<Category> findRootCategories();

    List<Category> findRootCategoriesWithI18n();

    List<Category> findByParentId(Long parentId);

    List<Category> findByIsVisibleTrue();

    Page<Category> findAllPaged(Pageable pageable);

    Page<Category> searchPaged(String query, Pageable pageable);

    Category save(Category entity);

    void delete(Category entity);

    void deleteById(Long id);

    boolean existsByCode(String code);

    boolean existsByUid(String uid);

    boolean hasChildren(Long categoryId);

    boolean hasProducts(Long categoryId);

    int findMaxSortOrderByParentId(Long parentId);
}
