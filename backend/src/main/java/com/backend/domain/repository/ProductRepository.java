package com.backend.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.domain.entity.Product;
import com.backend.domain.enums.ProductStatus;

public interface ProductRepository {

    Optional<Product> findById(Long id);

    Optional<Product> findByIdWithI18n(Long id);

    Optional<Product> findByIdWithAll(Long id);

    Optional<Product> findByUid(String uid);

    Optional<Product> findBySku(String sku);

    List<Product> findAll();

    Page<Product> findAllPaged(Pageable pageable);

    Page<Product> findByProductTypeId(Long productTypeId, Pageable pageable);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> searchPaged(String query, Pageable pageable);

    Page<Product> searchWithFilters(String query, Long productTypeId, Long categoryId, ProductStatus status,
            Pageable pageable);

    Page<Product> findPublishedByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findPublishedPaged(Pageable pageable);

    Page<Product> searchPublished(String query, Pageable pageable);

    Optional<Product> findByIdComposite(Long id);

    List<Product> findByCategoryId(Long categoryId);

    Page<Product> searchWithFilters(String query, ProductStatus status, Long categoryId, Pageable pageable);

    Product save(Product entity);

    void delete(Product entity);

    void deleteById(Long id);

    boolean existsBySku(String sku);

    boolean existsByUid(String uid);

    long countByProductTypeId(Long productTypeId);

    long countByCategoryId(Long categoryId);

    long count();

    long countByStatus(ProductStatus status);

    long countByCreatedAtAfter(LocalDateTime date);
}
