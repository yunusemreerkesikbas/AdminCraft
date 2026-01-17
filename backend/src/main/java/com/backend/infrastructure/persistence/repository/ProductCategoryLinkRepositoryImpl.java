package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.ProductCategoryLink;
import com.backend.domain.entity.ProductCategoryLinkId;
import com.backend.domain.repository.ProductCategoryLinkRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductCategoryLinkRepositoryImpl implements ProductCategoryLinkRepository {

    private final ProductCategoryLinkJpaRepository jpaRepository;

    @Override
    public Optional<ProductCategoryLink> findById(ProductCategoryLinkId id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ProductCategoryLink> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    public List<ProductCategoryLink> findByProductIdWithCategories(Long productId) {
        return jpaRepository.findByProductIdWithCategories(productId);
    }

    @Override
    public List<ProductCategoryLink> findByCategoryId(Long categoryId) {
        return jpaRepository.findByCategoryId(categoryId);
    }

    @Override
    public Optional<ProductCategoryLink> findPrimaryByProductId(Long productId) {
        return jpaRepository.findPrimaryByProductId(productId);
    }

    @Override
    public List<ProductCategoryLink> findPrimaryByProductIdIn(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findPrimaryByProductIdIn(productIds);
    }

    @Override
    @Transactional
    public ProductCategoryLink save(ProductCategoryLink entity) {
        return jpaRepository.save(entity);
    }

    @Override
    @Transactional
    public List<ProductCategoryLink> saveAll(List<ProductCategoryLink> entities) {
        return jpaRepository.saveAll(entities);
    }

    @Override
    @Transactional
    public void delete(ProductCategoryLink entity) {
        jpaRepository.delete(entity);
    }

    @Override
    @Transactional
    public void deleteByProductId(Long productId) {
        jpaRepository.deleteByProductId(productId);
    }

    @Override
    @Transactional
    public void deleteByProductIdAndCategoryId(Long productId, Long categoryId) {
        jpaRepository.deleteByProductIdAndCategoryId(productId, categoryId);
    }

    @Override
    public boolean existsByProductIdAndCategoryId(Long productId, Long categoryId) {
        return jpaRepository.existsByProductIdAndCategoryId(productId, categoryId);
    }

    @Override
    public long countByCategoryId(Long categoryId) {
        return jpaRepository.countByCategoryId(categoryId);
    }
}
