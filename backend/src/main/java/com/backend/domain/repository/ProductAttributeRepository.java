package com.backend.domain.repository;

import com.backend.domain.entity.ProductAttribute;

import java.util.List;
import java.util.Optional;

public interface ProductAttributeRepository {

    Optional<ProductAttribute> findById(Long id);

    Optional<ProductAttribute> findByProductIdAndAttributeDefinitionId(Long productId, Long attributeDefinitionId);

    List<ProductAttribute> findByProductId(Long productId);

    List<ProductAttribute> findByProductIdWithDefinitions(Long productId);

    ProductAttribute save(ProductAttribute entity);

    List<ProductAttribute> saveAll(List<ProductAttribute> entities);

    void delete(ProductAttribute entity);

    void deleteByProductId(Long productId);

    void deleteByProductIdAndAttributeDefinitionId(Long productId, Long attributeDefinitionId);
}
