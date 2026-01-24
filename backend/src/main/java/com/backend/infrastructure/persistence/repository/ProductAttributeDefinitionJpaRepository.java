package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ProductAttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface ProductAttributeDefinitionJpaRepository extends JpaRepository<ProductAttributeDefinition, Long> {

    Optional<ProductAttributeDefinition> findByUid(String uid);

    List<ProductAttributeDefinition> findByProductTypeId(Long productTypeId);

    Optional<ProductAttributeDefinition> findByProductTypeIdAndCode(Long productTypeId, String code);

    boolean existsByProductTypeIdAndCode(Long productTypeId, String code);

    @Modifying
    @Query("DELETE FROM ProductAttributeDefinition pad WHERE pad.productType.id = :productTypeId")
    void deleteByProductTypeId(@Param("productTypeId") Long productTypeId);
}
