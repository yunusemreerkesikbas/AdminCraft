package com.backend.domain.repository;

import com.backend.domain.entity.ProductAttributeDefinition;

import java.util.List;
import java.util.Optional;

public interface ProductAttributeDefinitionRepository {

    Optional<ProductAttributeDefinition> findById(Long id);

    Optional<ProductAttributeDefinition> findByUid(String uid);

    List<ProductAttributeDefinition> findByProductTypeId(Long productTypeId);

    List<ProductAttributeDefinition> findByProductTypeIdOrderBySortOrder(Long productTypeId);

    Optional<ProductAttributeDefinition> findByProductTypeIdAndCode(Long productTypeId, String code);

    ProductAttributeDefinition save(ProductAttributeDefinition entity);

    void delete(ProductAttributeDefinition entity);

    void deleteById(Long id);

    void deleteByProductTypeId(Long productTypeId);

    boolean existsByProductTypeIdAndCode(Long productTypeId, String code);

    int findMaxSortOrderByProductTypeId(Long productTypeId);
}
