package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ProductCategoryLink;
import com.backend.domain.entity.ProductCategoryLinkId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface ProductCategoryLinkJpaRepository extends JpaRepository<ProductCategoryLink, ProductCategoryLinkId> {

    List<ProductCategoryLink> findByProductId(Long productId);

    @EntityGraph(attributePaths = {"category", "category.i18nContent"})
    @Query("SELECT pcl FROM ProductCategoryLink pcl WHERE pcl.product.id = :productId")
    List<ProductCategoryLink> findByProductIdWithCategories(@Param("productId") Long productId);

    List<ProductCategoryLink> findByCategoryId(Long categoryId);

    @Query("SELECT pcl FROM ProductCategoryLink pcl WHERE pcl.product.id = :productId AND pcl.isPrimary = true")
    Optional<ProductCategoryLink> findPrimaryByProductId(@Param("productId") Long productId);

    boolean existsByProductIdAndCategoryId(Long productId, Long categoryId);

    long countByCategoryId(Long categoryId);

    @Modifying
    @Query("DELETE FROM ProductCategoryLink pcl WHERE pcl.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    @Modifying
    @Query("DELETE FROM ProductCategoryLink pcl WHERE pcl.product.id = :productId AND pcl.category.id = :categoryId")
    void deleteByProductIdAndCategoryId(@Param("productId") Long productId, @Param("categoryId") Long categoryId);
}
