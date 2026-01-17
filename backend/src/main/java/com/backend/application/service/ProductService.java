package com.backend.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.application.dto.request.ProductI18nDto;
import com.backend.domain.entity.Product;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProductStatus;

public interface ProductService {

    Product createComposite(Long productTypeId, String sku, BigDecimal basePrice, String currency,
            ProductStatus status, Boolean isVisible, Long responsiveMediaId,
            Map<Language, ProductI18nDto> translations,
            Map<String, Object> attributes,
            List<Long> categoryIds, Long primaryCategoryId,
            List<Long> galleryMediaIds,
            Long createdBy);

    Product updateComposite(Long id, BigDecimal basePrice, String currency,
            ProductStatus status, Boolean isVisible, Long responsiveMediaId,
            Map<Language, ProductI18nDto> translations,
            Map<String, Object> attributes,
            List<Long> categoryIds, Long primaryCategoryId,
            List<Long> galleryMediaIds,
            Long updatedBy);

    void delete(Long id);

    Optional<Product> findById(Long id);

    Optional<Product> findByIdComposite(Long id);

    Optional<Product> findByUid(String uid);

    Optional<Product> findBySku(String sku);

    List<Product> findAll();

    Page<Product> findAllPaged(Pageable pageable);

    Page<Product> search(String query, ProductStatus status, Long categoryId, Pageable pageable);

    Map<Long, String> getPrimaryCategoryNamesForProducts(List<Long> productIds, Language language);

    List<Product> findByCategoryId(Long categoryId);

    Product updateStatus(Long id, ProductStatus status, Long updatedBy);

    Product updateVisibility(Long id, Boolean isVisible, Long updatedBy);
}
