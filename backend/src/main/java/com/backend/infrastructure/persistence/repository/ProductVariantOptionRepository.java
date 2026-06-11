package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.entity.ProductVariantOption;

public interface ProductVariantOptionRepository extends JpaRepository<ProductVariantOption, Long> {

    @EntityGraph(attributePaths = { "values" })
    List<ProductVariantOption> findAllByOrderBySortOrderAscIdAsc();

    @EntityGraph(attributePaths = { "values" })
    @Query("SELECT o FROM ProductVariantOption o WHERE o.id = :id")
    Optional<ProductVariantOption> findByIdWithValues(@Param("id") Long id);

    boolean existsByCode(String code);
}
