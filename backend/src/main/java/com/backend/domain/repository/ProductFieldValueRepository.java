package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.ProductFieldValue;

/**
 * Repository for ProductFieldValue entity.
 */
@Repository
public interface ProductFieldValueRepository extends JpaRepository<ProductFieldValue, Long> {

  List<ProductFieldValue> findByProductId(Long productId);

  @Query("SELECT pfv FROM ProductFieldValue pfv " +
      "JOIN FETCH pfv.fieldDefinition " +
      "WHERE pfv.product.id = :productId")
  List<ProductFieldValue> findByProductIdWithDefinition(@Param("productId") Long productId);

  Optional<ProductFieldValue> findByProductIdAndFieldDefinitionId(Long productId, Long fieldDefinitionId);

  void deleteByProductId(Long productId);

  void deleteByFieldDefinitionId(Long fieldDefinitionId);

  List<ProductFieldValue> findByFieldDefinitionId(Long fieldDefinitionId);

  boolean existsByFieldDefinitionId(Long fieldDefinitionId);
}
