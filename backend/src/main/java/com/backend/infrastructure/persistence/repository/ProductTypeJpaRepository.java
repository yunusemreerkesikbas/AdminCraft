package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface ProductTypeJpaRepository extends JpaRepository<ProductType, Long> {

    Optional<ProductType> findByUid(String uid);

    Optional<ProductType> findByCode(String code);

    @EntityGraph(attributePaths = {"attributeDefinitions"})
    @Query("SELECT pt FROM ProductType pt WHERE pt.id = :id")
    Optional<ProductType> findByIdWithAttributes(@Param("id") Long id);

    List<ProductType> findByCategory(String category);

    boolean existsByCode(String code);

    boolean existsByUid(String uid);

    @Query(value = "SELECT pt FROM ProductType pt",
            countQuery = "SELECT COUNT(pt) FROM ProductType pt")
    Page<ProductType> findAllPaged(Pageable pageable);

    @Query(value = "SELECT pt FROM ProductType pt " +
            "WHERE LOWER(pt.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(pt.code) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(pt.category) LIKE LOWER(CONCAT('%', :query, '%'))",
            countQuery = "SELECT COUNT(pt) FROM ProductType pt " +
                    "WHERE LOWER(pt.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
                    "OR LOWER(pt.code) LIKE LOWER(CONCAT('%', :query, '%')) " +
                    "OR LOWER(pt.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<ProductType> searchByQuery(@Param("query") String query, Pageable pageable);
}
