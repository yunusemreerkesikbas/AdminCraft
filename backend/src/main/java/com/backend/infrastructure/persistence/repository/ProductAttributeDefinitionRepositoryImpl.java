package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ProductAttributeDefinition;
import com.backend.domain.repository.ProductAttributeDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductAttributeDefinitionRepositoryImpl implements ProductAttributeDefinitionRepository {

    private final ProductAttributeDefinitionJpaRepository jpaRepository;

    @Override
    public Optional<ProductAttributeDefinition> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ProductAttributeDefinition> findByUid(String uid) {
        return jpaRepository.findByUid(uid);
    }

    @Override
    public List<ProductAttributeDefinition> findByProductTypeId(Long productTypeId) {
        return jpaRepository.findByProductTypeId(productTypeId);
    }

    @Override
    public Optional<ProductAttributeDefinition> findByProductTypeIdAndCode(Long productTypeId, String code) {
        return jpaRepository.findByProductTypeIdAndCode(productTypeId, code);
    }

    @Override
    public ProductAttributeDefinition save(ProductAttributeDefinition entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public void delete(ProductAttributeDefinition entity) {
        jpaRepository.delete(entity);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByProductTypeId(Long productTypeId) {
        jpaRepository.deleteByProductTypeId(productTypeId);
    }

    @Override
    public boolean existsByProductTypeIdAndCode(Long productTypeId, String code) {
        return jpaRepository.existsByProductTypeIdAndCode(productTypeId, code);
    }
}
