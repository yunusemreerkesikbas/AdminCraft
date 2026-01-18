package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.ProductMedia;

interface ProductMediaJpaRepository extends JpaRepository<ProductMedia, Long> {

    @Query("SELECT pm FROM ProductMedia pm JOIN pm.responsiveMediaSet rms " +
            "WHERE pm.product.id = :productId AND (rms.desktopMedia.id = :mediaId OR rms.mobileMedia.id = :mediaId)")
    Optional<ProductMedia> findByProductIdAndMediaId(@Param("productId") Long productId,
            @Param("mediaId") Long mediaId);

    List<ProductMedia> findByProductId(Long productId);

    @EntityGraph(attributePaths = { "responsiveMediaSet", "responsiveMediaSet.desktopMedia",
            "responsiveMediaSet.mobileMedia" })
    @Query("SELECT pm FROM ProductMedia pm WHERE pm.product.id = :productId")
    List<ProductMedia> findByProductIdWithMedia(@Param("productId") Long productId);

    List<ProductMedia> findByProductIdOrderBySortOrderAsc(Long productId);

    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END FROM ProductMedia pm JOIN pm.responsiveMediaSet rms "
            +
            "WHERE pm.product.id = :productId AND (rms.desktopMedia.id = :mediaId OR rms.mobileMedia.id = :mediaId)")
    boolean existsByProductIdAndMediaId(@Param("productId") Long productId, @Param("mediaId") Long mediaId);

    @Query("SELECT COALESCE(MAX(pm.sortOrder), 0) FROM ProductMedia pm WHERE pm.product.id = :productId")
    int findMaxSortOrderByProductId(@Param("productId") Long productId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductMedia pm WHERE pm.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductMedia pm WHERE pm.product.id = :productId AND pm.responsiveMediaSet.id IN " +
            "(SELECT rms.id FROM ResponsiveMediaSet rms WHERE rms.desktopMedia.id = :mediaId OR rms.mobileMedia.id = :mediaId)")
    void deleteByProductIdAndMediaId(@Param("productId") Long productId, @Param("mediaId") Long mediaId);
}
