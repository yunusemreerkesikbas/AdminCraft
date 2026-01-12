package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.ProductAttribute;
import com.backend.domain.repository.ProductAttributeRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductAttributeRepositoryImpl implements ProductAttributeRepository {

    private final ProductAttributeJpaRepository jpaRepository;

    @Override
    public Optional<ProductAttribute> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ProductAttribute> findByProductIdAndAttributeDefinitionId(Long productId,
            Long attributeDefinitionId) {
        return jpaRepository.findByProductIdAndAttributeDefinitionId(productId, attributeDefinitionId);
    }

    @Override
    public List<ProductAttribute> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    public List<ProductAttribute> findByProductIdWithDefinitions(Long productId) {
        return jpaRepository.findByProductIdWithDefinitions(productId);
    }

    @Override
    @Transactional
    public ProductAttribute save(ProductAttribute entity) {
        return jpaRepository.save(entity);
    }

    @Override
    @Transactional
    public List<ProductAttribute> saveAll(List<ProductAttribute> entities) {
        return jpaRepository.saveAll(entities);
    }

    @Override
    @Transactional
    public void delete(ProductAttribute entity) {
        jpaRepository.delete(entity);
    }

    @Override
    @Transactional
    public void deleteByProductId(Long productId) {
        jpaRepository.deleteByProductId(productId);
    }

    @Override
    @Transactional
    public void deleteByProductIdAndAttributeDefinitionId(Long productId, Long attributeDefinitionId) {
        jpaRepository.deleteByProductIdAndAttributeDefinitionId(productId, attributeDefinitionId);
    }
}
