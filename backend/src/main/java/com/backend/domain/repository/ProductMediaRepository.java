package com.backend.domain.repository;

import com.backend.domain.entity.ProductMedia;

import java.util.List;
import java.util.Optional;

public interface ProductMediaRepository {

    Optional<ProductMedia> findById(Long id);

    Optional<ProductMedia> findByProductIdAndMediaId(Long productId, Long mediaId);

    List<ProductMedia> findByProductId(Long productId);

    List<ProductMedia> findByProductIdWithMedia(Long productId);

    List<ProductMedia> findByProductIdOrderBySortOrder(Long productId);

    ProductMedia save(ProductMedia entity);

    List<ProductMedia> saveAll(List<ProductMedia> entities);

    void delete(ProductMedia entity);

    void deleteById(Long id);

    void deleteByProductId(Long productId);

    void deleteByProductIdAndMediaId(Long productId, Long mediaId);

    boolean existsByProductIdAndMediaId(Long productId, Long mediaId);

    int findMaxSortOrderByProductId(Long productId);
}
