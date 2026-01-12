package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface CategoryJpaRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByUid(String uid);

    Optional<Category> findByCode(String code);

    @EntityGraph(attributePaths = {"i18nContent"})
    @Query("SELECT c FROM Category c WHERE c.id = :id")
    Optional<Category> findByIdWithI18n(@Param("id") Long id);

    @EntityGraph(attributePaths = {"children"})
    @Query("SELECT c FROM Category c WHERE c.id = :id")
    Optional<Category> findByIdWithChildren(@Param("id") Long id);

    @Query("SELECT c FROM Category c WHERE c.parentId IS NULL ORDER BY c.sortOrder ASC, c.id ASC")
    List<Category> findRootCategories();

    List<Category> findByParentIdOrderBySortOrderAsc(Long parentId);

    List<Category> findByIsVisibleTrue();

    boolean existsByCode(String code);

    boolean existsByUid(String uid);

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.parentId = :categoryId")
    boolean hasChildren(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(pcl) > 0 FROM ProductCategoryLink pcl WHERE pcl.category.id = :categoryId")
    boolean hasProducts(@Param("categoryId") Long categoryId);

    @Query("SELECT COALESCE(MAX(c.sortOrder), 0) FROM Category c WHERE c.parentId = :parentId")
    int findMaxSortOrderByParentId(@Param("parentId") Long parentId);

    @Query(value = "SELECT c FROM Category c",
            countQuery = "SELECT COUNT(c) FROM Category c")
    Page<Category> findAllPaged(Pageable pageable);

    @Query(value = "SELECT DISTINCT c FROM Category c LEFT JOIN c.i18nContent i " +
            "WHERE LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%'))",
            countQuery = "SELECT COUNT(DISTINCT c) FROM Category c LEFT JOIN c.i18nContent i " +
                    "WHERE LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%')) " +
                    "OR LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Category> searchByQuery(@Param("query") String query, Pageable pageable);
}
