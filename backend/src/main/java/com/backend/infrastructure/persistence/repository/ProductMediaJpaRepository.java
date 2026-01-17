package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ProductMedia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

interface ProductMediaJpaRepository extends JpaRepository<ProductMedia, Long> {

    Optional<ProductMedia> findByProductIdAndMediaId(Long productId, Long mediaId);

    List<ProductMedia> findByProductId(Long productId);

    @EntityGraph(attributePaths = {"media"})
    @Query("SELECT pm FROM ProductMedia pm WHERE pm.product.id = :productId")
    List<ProductMedia> findByProductIdWithMedia(@Param("productId") Long productId);

    List<ProductMedia> findByProductIdOrderBySortOrderAsc(Long productId);

    boolean existsByProductIdAndMediaId(Long productId, Long mediaId);

    @Query("SELECT COALESCE(MAX(pm.sortOrder), 0) FROM ProductMedia pm WHERE pm.product.id = :productId")
    int findMaxSortOrderByProductId(@Param("productId") Long productId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductMedia pm WHERE pm.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductMedia pm WHERE pm.product.id = :productId AND pm.media.id = :mediaId")
    void deleteByProductIdAndMediaId(@Param("productId") Long productId, @Param("mediaId") Long mediaId);
}
