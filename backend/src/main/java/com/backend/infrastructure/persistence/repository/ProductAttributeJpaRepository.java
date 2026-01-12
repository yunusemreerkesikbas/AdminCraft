package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ProductAttribute;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface ProductAttributeJpaRepository extends JpaRepository<ProductAttribute, Long> {

    Optional<ProductAttribute> findByProductIdAndAttributeDefinitionId(Long productId, Long attributeDefinitionId);

    List<ProductAttribute> findByProductId(Long productId);

    @EntityGraph(attributePaths = {"attributeDefinition"})
    @Query("SELECT pa FROM ProductAttribute pa WHERE pa.product.id = :productId")
    List<ProductAttribute> findByProductIdWithDefinitions(@Param("productId") Long productId);

    @Modifying
    @Query("DELETE FROM ProductAttribute pa WHERE pa.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    @Modifying
    @Query("DELETE FROM ProductAttribute pa WHERE pa.product.id = :productId AND pa.attributeDefinition.id = :attributeDefinitionId")
    void deleteByProductIdAndAttributeDefinitionId(@Param("productId") Long productId, @Param("attributeDefinitionId") Long attributeDefinitionId);
}
