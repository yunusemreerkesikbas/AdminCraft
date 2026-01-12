package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.ProductType;
import com.backend.domain.repository.ProductTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductTypeRepositoryImpl implements ProductTypeRepository {

    private final ProductTypeJpaRepository jpaRepository;

    @Override
    public Optional<ProductType> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ProductType> findByIdWithAttributes(Long id) {
        return jpaRepository.findByIdWithAttributes(id);
    }

    @Override
    public Optional<ProductType> findByUid(String uid) {
        return jpaRepository.findByUid(uid);
    }

    @Override
    public Optional<ProductType> findByCode(String code) {
        return jpaRepository.findByCode(code);
    }

    @Override
    public List<ProductType> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Page<ProductType> findAllPaged(Pageable pageable) {
        return jpaRepository.findAllPaged(pageable);
    }

    @Override
    public Page<ProductType> searchPaged(String query, Pageable pageable) {
        return jpaRepository.searchByQuery(query, pageable);
    }

    @Override
    public List<ProductType> findByCategory(String category) {
        return jpaRepository.findByCategory(category);
    }

    @Override
    public ProductType save(ProductType entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public void delete(ProductType entity) {
        jpaRepository.delete(entity);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsByUid(String uid) {
        return jpaRepository.existsByUid(uid);
    }
}
