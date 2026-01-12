package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.ProductMedia;
import com.backend.domain.repository.ProductMediaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductMediaRepositoryImpl implements ProductMediaRepository {

    private final ProductMediaJpaRepository jpaRepository;

    @Override
    public Optional<ProductMedia> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ProductMedia> findByProductIdAndMediaId(Long productId, Long mediaId) {
        return jpaRepository.findByProductIdAndMediaId(productId, mediaId);
    }

    @Override
    public List<ProductMedia> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    public List<ProductMedia> findByProductIdWithMedia(Long productId) {
        return jpaRepository.findByProductIdWithMedia(productId);
    }

    @Override
    public List<ProductMedia> findByProductIdOrderBySortOrder(Long productId) {
        return jpaRepository.findByProductIdOrderBySortOrderAsc(productId);
    }

    @Override
    @Transactional
    public ProductMedia save(ProductMedia entity) {
        return jpaRepository.save(entity);
    }

    @Override
    @Transactional
    public List<ProductMedia> saveAll(List<ProductMedia> entities) {
        return jpaRepository.saveAll(entities);
    }

    @Override
    @Transactional
    public void delete(ProductMedia entity) {
        jpaRepository.delete(entity);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByProductId(Long productId) {
        jpaRepository.deleteByProductId(productId);
    }

    @Override
    @Transactional
    public void deleteByProductIdAndMediaId(Long productId, Long mediaId) {
        jpaRepository.deleteByProductIdAndMediaId(productId, mediaId);
    }

    @Override
    public boolean existsByProductIdAndMediaId(Long productId, Long mediaId) {
        return jpaRepository.existsByProductIdAndMediaId(productId, mediaId);
    }

    @Override
    public int findMaxSortOrderByProductId(Long productId) {
        return jpaRepository.findMaxSortOrderByProductId(productId);
    }
}
