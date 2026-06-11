package com.backend.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.entity.Product;
import com.backend.domain.enums.ProductStatus;

interface ProductJpaRepository extends JpaRepository<Product, Long> {

        Optional<Product> findByUid(String uid);

        Optional<Product> findBySku(String sku);

        @EntityGraph(attributePaths = { "i18nContent" })
        @Query("SELECT p FROM Product p WHERE p.id = :id")
        Optional<Product> findByIdWithI18n(@Param("id") Long id);

        @EntityGraph(attributePaths = { "productType", "responsiveMediaSet", "i18nContent", "attributes",
                        "categoryLinks", "gallery" })
        @Query("SELECT p FROM Product p WHERE p.id = :id")
        Optional<Product> findByIdWithAll(@Param("id") Long id);

        boolean existsBySku(String sku);

        boolean existsByUid(String uid);

        long countByProductTypeId(Long productTypeId);

        @Query(value = "SELECT p FROM Product p", countQuery = "SELECT COUNT(p) FROM Product p")
        Page<Product> findAllPaged(Pageable pageable);

        Page<Product> findByProductTypeId(Long productTypeId, Pageable pageable);

        Page<Product> findByStatus(ProductStatus status, Pageable pageable);

        @Query(value = "SELECT DISTINCT p FROM Product p JOIN p.categoryLinks pcl WHERE pcl.category.id = :categoryId", countQuery = "SELECT COUNT(DISTINCT p) FROM Product p JOIN p.categoryLinks pcl WHERE pcl.category.id = :categoryId")
        Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

        @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN p.i18nContent i " +
                        "WHERE LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%'))", countQuery = "SELECT COUNT(DISTINCT p) FROM Product p LEFT JOIN p.i18nContent i "
                                        +
                                        "WHERE LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) " +
                                        "OR LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%'))")
        Page<Product> searchByQuery(@Param("query") String query, Pageable pageable);

        @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN p.i18nContent i LEFT JOIN p.categoryLinks pcl " +
                        "WHERE (:query IS NULL OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%'))) "
                        +
                        "AND (:productTypeId IS NULL OR p.productType.id = :productTypeId) " +
                        "AND (:categoryId IS NULL OR pcl.category.id = :categoryId) " +
                        "AND (:status IS NULL OR p.status = :status)", countQuery = "SELECT COUNT(DISTINCT p) FROM Product p LEFT JOIN p.i18nContent i LEFT JOIN p.categoryLinks pcl "
                                        +
                                        "WHERE (:query IS NULL OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%'))) "
                                        +
                                        "AND (:productTypeId IS NULL OR p.productType.id = :productTypeId) " +
                                        "AND (:categoryId IS NULL OR pcl.category.id = :categoryId) " +
                                        "AND (:status IS NULL OR p.status = :status)")
        Page<Product> searchWithFilters(@Param("query") String query,
                        @Param("productTypeId") Long productTypeId,
                        @Param("categoryId") Long categoryId,
                        @Param("status") ProductStatus status,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT p FROM Product p JOIN p.categoryLinks pcl " +
                        "WHERE pcl.category.id = :categoryId AND p.status = 'PUBLISHED' AND p.isVisible = true", countQuery = "SELECT COUNT(DISTINCT p) FROM Product p JOIN p.categoryLinks pcl "
                                        +
                                        "WHERE pcl.category.id = :categoryId AND p.status = 'PUBLISHED' AND p.isVisible = true")
        Page<Product> findPublishedByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

        @Query(value = "SELECT p FROM Product p WHERE p.status = 'PUBLISHED' AND p.isVisible = true", countQuery = "SELECT COUNT(p) FROM Product p WHERE p.status = 'PUBLISHED' AND p.isVisible = true")
        Page<Product> findPublishedPaged(Pageable pageable);

        @Query("SELECT COUNT(p) > 0 FROM Product p JOIN p.categoryLinks pcl WHERE pcl.category.id = :categoryId")
        boolean existsByCategoryId(@Param("categoryId") Long categoryId);

        long countByStatus(ProductStatus status);

        long countByCreatedAtAfter(LocalDateTime date);

        @Query("SELECT COUNT(pcl) FROM ProductCategoryLink pcl WHERE pcl.category.id = :categoryId")
        long countByCategoryId(@Param("categoryId") Long categoryId);

        @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN p.i18nContent i " +
                        "WHERE p.status = 'PUBLISHED' AND p.isVisible = true " +
                        "AND (LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(i.shortDescription) LIKE LOWER(CONCAT('%', :query, '%')))", countQuery = "SELECT COUNT(DISTINCT p) FROM Product p LEFT JOIN p.i18nContent i "
                                        +
                                        "WHERE p.status = 'PUBLISHED' AND p.isVisible = true " +
                                        "AND (LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) " +
                                        "OR LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
                                        "OR LOWER(i.shortDescription) LIKE LOWER(CONCAT('%', :query, '%')))")
        Page<Product> searchPublished(@Param("query") String query, Pageable pageable);

        @EntityGraph(attributePaths = { "productType", "responsiveMediaSet", "responsiveMediaSet.desktopMedia",
                        "responsiveMediaSet.mobileMedia", "attributes",
                        "attributes.attributeDefinition", "gallery", "gallery.responsiveMediaSet",
                        "gallery.responsiveMediaSet.desktopMedia", "gallery.responsiveMediaSet.mobileMedia",
                        "variants", "variants.optionValues", "variants.optionValues.option",
                        "variants.responsiveMediaSet" })
        @Query("SELECT p FROM Product p WHERE p.id = :id")
        Optional<Product> findByIdComposite(@Param("id") Long id);

        @Query("SELECT DISTINCT p FROM Product p JOIN p.categoryLinks pcl WHERE pcl.category.id = :categoryId")
        java.util.List<Product> findByCategoryIdList(@Param("categoryId") Long categoryId);

        @EntityGraph(attributePaths = { "productType", "i18nContent", "responsiveMediaSet",
                        "responsiveMediaSet.desktopMedia",
                        "responsiveMediaSet.mobileMedia" })
        @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN p.i18nContent i LEFT JOIN p.categoryLinks pcl " +
                        "WHERE (:query IS NULL OR :query = '' OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%'))) "
                        +
                        "AND (:status IS NULL OR p.status = :status) " +
                        "AND (:categoryId IS NULL OR pcl.category.id = :categoryId)", countQuery = "SELECT COUNT(DISTINCT p) FROM Product p LEFT JOIN p.i18nContent i LEFT JOIN p.categoryLinks pcl "
                                        +
                                        "WHERE (:query IS NULL OR :query = '' OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%'))) "
                                        +
                                        "AND (:status IS NULL OR p.status = :status) " +
                                        "AND (:categoryId IS NULL OR pcl.category.id = :categoryId)")
        Page<Product> searchWithStatusAndCategory(@Param("query") String query,
                        @Param("status") ProductStatus status,
                        @Param("categoryId") Long categoryId,
                        Pageable pageable);
}
