package com.backend.domain.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.entity.ProductVariantOptionValue;

public interface ProductVariantOptionValueRepository extends JpaRepository<ProductVariantOptionValue, Long> {

    @EntityGraph(attributePaths = { "option" })
    List<ProductVariantOptionValue> findByIdIn(Collection<Long> ids);
}

