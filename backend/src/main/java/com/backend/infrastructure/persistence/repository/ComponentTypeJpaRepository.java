package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.entity.ComponentType;

interface ComponentTypeJpaRepository extends JpaRepository<ComponentType, Long> {
    Optional<ComponentType> findByUuid(String uuid);

    Optional<ComponentType> findByUid(String uid);

    List<ComponentType> findByCategory(String category);

    List<ComponentType> findByIdIn(List<Long> ids);

    boolean existsByUid(String uid);

    @Query(value = "SELECT ct FROM ComponentType ct",
            countQuery = "SELECT COUNT(ct) FROM ComponentType ct")
    Page<ComponentType> findAllPaged(Pageable pageable);

    @Query(value = "SELECT ct FROM ComponentType ct " +
            "WHERE LOWER(ct.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(ct.uid) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(ct.category) LIKE LOWER(CONCAT('%', :query, '%'))",
            countQuery = "SELECT COUNT(ct) FROM ComponentType ct " +
            "WHERE LOWER(ct.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(ct.uid) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(ct.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<ComponentType> searchByQuery(@Param("query") String query, Pageable pageable);
}
