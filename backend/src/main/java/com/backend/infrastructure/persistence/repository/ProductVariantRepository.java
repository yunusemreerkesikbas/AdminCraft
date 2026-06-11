package com.backend.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.entity.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @EntityGraph(attributePaths = { "optionValues", "optionValues.option", "responsiveMediaSet" })
    List<ProductVariant> findByProductIdOrderByIdAsc(Long productId);

    boolean existsBySku(String sku);

    boolean existsBySkuAndProductIdNot(String sku, Long productId);

    void deleteByProductId(Long productId);

    @Query("SELECT COUNT(v) > 0 FROM ProductVariant v JOIN v.optionValues optionValue WHERE optionValue.option.id = :optionId")
    boolean existsByOptionId(@Param("optionId") Long optionId);

    @Query("SELECT DISTINCT optionValue.id FROM ProductVariant v JOIN v.optionValues optionValue WHERE optionValue.id IN :valueIds")
    List<Long> findUsedOptionValueIds(@Param("valueIds") Collection<Long> valueIds);
}
