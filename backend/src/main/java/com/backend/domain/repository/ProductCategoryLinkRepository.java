package com.backend.domain.repository;

import com.backend.domain.entity.ProductCategoryLink;
import com.backend.domain.entity.ProductCategoryLinkId;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryLinkRepository {

    Optional<ProductCategoryLink> findById(ProductCategoryLinkId id);

    List<ProductCategoryLink> findByProductId(Long productId);

    List<ProductCategoryLink> findByProductIdWithCategories(Long productId);

    List<ProductCategoryLink> findByCategoryId(Long categoryId);

    Optional<ProductCategoryLink> findPrimaryByProductId(Long productId);

    List<ProductCategoryLink> findPrimaryByProductIdIn(List<Long> productIds);

    ProductCategoryLink save(ProductCategoryLink entity);

    List<ProductCategoryLink> saveAll(List<ProductCategoryLink> entities);

    void delete(ProductCategoryLink entity);

    void deleteByProductId(Long productId);

    void deleteByProductIdAndCategoryId(Long productId, Long categoryId);

    boolean existsByProductIdAndCategoryId(Long productId, Long categoryId);

    long countByCategoryId(Long categoryId);
}
