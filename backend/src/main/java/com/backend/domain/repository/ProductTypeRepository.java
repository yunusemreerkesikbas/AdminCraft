package com.backend.domain.repository;

import com.backend.domain.entity.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductTypeRepository {

    Optional<ProductType> findById(Long id);

    Optional<ProductType> findByIdWithAttributes(Long id);

    Optional<ProductType> findByUid(String uid);

    Optional<ProductType> findByCode(String code);

    List<ProductType> findAll();

    Page<ProductType> findAllPaged(Pageable pageable);

    Page<ProductType> searchPaged(String query, Pageable pageable);

    List<ProductType> findByCategory(String category);

    ProductType save(ProductType entity);

    void delete(ProductType entity);

    void deleteById(Long id);

    boolean existsByCode(String code);

    boolean existsByUid(String uid);
}
