package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.ProductFieldDefinition;

/**
 * Repository for ProductFieldDefinition entity.
 */
@Repository
public interface ProductFieldDefinitionRepository extends JpaRepository<ProductFieldDefinition, Long> {

  Optional<ProductFieldDefinition> findByUuid(String uuid);

  Optional<ProductFieldDefinition> findByUid(String uid);

  Optional<ProductFieldDefinition> findByCode(String code);

  boolean existsByCode(String code);

  boolean existsByUid(String uid);

  List<ProductFieldDefinition> findByCodeIn(List<String> codes);
}
