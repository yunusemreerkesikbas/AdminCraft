package com.backend.application.service;

import com.backend.domain.entity.ProductAttributeDefinition;
import com.backend.domain.entity.ProductType;
import com.backend.domain.enums.ProductFieldType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductTypeService {

    ProductType create(String code, String name, String category, Long createdBy);

    ProductType update(Long id, String name, String category, Long updatedBy);

    void delete(Long id);

    Optional<ProductType> findById(Long id);

    Optional<ProductType> findByIdWithAttributes(Long id);

    Optional<ProductType> findByUid(String uid);

    Optional<ProductType> findByCode(String code);

    List<ProductType> findAll();

    Page<ProductType> findAllPaged(Pageable pageable);

    Page<ProductType> search(String query, Pageable pageable);

    List<ProductType> findByCategory(String category);

    ProductAttributeDefinition addAttribute(Long productTypeId, String code, String name,
            ProductFieldType fieldType, Boolean isRequired, Boolean isSearchable,
            Integer sortOrder, String validationConfig);

    ProductAttributeDefinition updateAttribute(Long productTypeId, Long attributeId,
            String name, ProductFieldType fieldType, Boolean isRequired,
            Boolean isSearchable, Integer sortOrder, String validationConfig);

    void deleteAttribute(Long productTypeId, Long attributeId);

    List<ProductAttributeDefinition> getAttributes(Long productTypeId);
}
