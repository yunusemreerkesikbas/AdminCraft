package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Product;
import com.backend.domain.enums.ProductStatus;
import com.backend.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Product> findByIdWithI18n(Long id) {
        return jpaRepository.findByIdWithI18n(id);
    }

    @Override
    public Optional<Product> findByIdWithAll(Long id) {
        return jpaRepository.findByIdWithAll(id);
    }

    @Override
    public Optional<Product> findByUid(String uid) {
        return jpaRepository.findByUid(uid);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return jpaRepository.findBySku(sku);
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Page<Product> findAllPaged(Pageable pageable) {
        return jpaRepository.findAllPaged(pageable);
    }

    @Override
    public Page<Product> findByProductTypeId(Long productTypeId, Pageable pageable) {
        return jpaRepository.findByProductTypeId(productTypeId, pageable);
    }

    @Override
    public Page<Product> findByStatus(ProductStatus status, Pageable pageable) {
        return jpaRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Product> findByCategoryId(Long categoryId, Pageable pageable) {
        return jpaRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Page<Product> searchPaged(String query, Pageable pageable) {
        return jpaRepository.searchByQuery(query, pageable);
    }

    @Override
    public Page<Product> searchWithFilters(String query, Long productTypeId, Long categoryId, ProductStatus status, Pageable pageable) {
        return jpaRepository.searchWithFilters(query, productTypeId, categoryId, status, pageable);
    }

    @Override
    public Page<Product> findPublishedByCategoryId(Long categoryId, Pageable pageable) {
        return jpaRepository.findPublishedByCategoryId(categoryId, pageable);
    }

    @Override
    public Page<Product> findPublishedPaged(Pageable pageable) {
        return jpaRepository.findPublishedPaged(pageable);
    }

    @Override
    public Product save(Product entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public void delete(Product entity) {
        jpaRepository.delete(entity);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }

    @Override
    public boolean existsByUid(String uid) {
        return jpaRepository.existsByUid(uid);
    }

    @Override
    public long countByProductTypeId(Long productTypeId) {
        return jpaRepository.countByProductTypeId(productTypeId);
    }

    @Override
    public long countByCategoryId(Long categoryId) {
        return jpaRepository.countByCategoryId(categoryId);
    }

    @Override
    public Page<Product> searchPublished(String query, Pageable pageable) {
        return jpaRepository.searchPublished(query, pageable);
    }

    @Override
    public Optional<Product> findByIdComposite(Long id) {
        return jpaRepository.findByIdComposite(id);
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        return jpaRepository.findByCategoryIdList(categoryId);
    }

    @Override
    public Page<Product> searchWithFilters(String query, ProductStatus status, Long categoryId, Pageable pageable) {
        return jpaRepository.searchWithStatusAndCategory(query, status, categoryId, pageable);
    }
}
