package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.Category;
import com.backend.domain.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    @Override
    public Optional<Category> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Category> findByIdWithI18n(Long id) {
        return jpaRepository.findByIdWithI18n(id);
    }

    @Override
    public Optional<Category> findByIdWithChildren(Long id) {
        return jpaRepository.findByIdWithChildren(id);
    }

    @Override
    public Optional<Category> findByUid(String uid) {
        return jpaRepository.findByUid(uid);
    }

    @Override
    public Optional<Category> findByCode(String code) {
        return jpaRepository.findByCode(code);
    }

    @Override
    public List<Category> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Category> findRootCategories() {
        return jpaRepository.findRootCategories();
    }

    @Override
    public List<Category> findByParentId(Long parentId) {
        return jpaRepository.findByParentIdOrderBySortOrderAsc(parentId);
    }

    @Override
    public List<Category> findByIsVisibleTrue() {
        return jpaRepository.findByIsVisibleTrue();
    }

    @Override
    public Page<Category> findAllPaged(Pageable pageable) {
        return jpaRepository.findAllPaged(pageable);
    }

    @Override
    public Page<Category> searchPaged(String query, Pageable pageable) {
        return jpaRepository.searchByQuery(query, pageable);
    }

    @Override
    @Transactional
    public Category save(Category entity) {
        return jpaRepository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Category entity) {
        jpaRepository.delete(entity);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsByUid(String uid) {
        return jpaRepository.existsByUid(uid);
    }

    @Override
    public boolean hasChildren(Long categoryId) {
        return jpaRepository.hasChildren(categoryId);
    }

    @Override
    public boolean hasProducts(Long categoryId) {
        return jpaRepository.hasProducts(categoryId);
    }

    @Override
    public int findMaxSortOrderByParentId(Long parentId) {
        return jpaRepository.findMaxSortOrderByParentId(parentId);
    }
}
