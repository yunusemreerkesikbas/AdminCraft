package com.backend.testutil.builders;

import com.backend.domain.entity.Product;
import com.backend.domain.entity.ProductType;
import com.backend.domain.entity.ResponsiveMediaSet;
import com.backend.domain.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test data builder for Product entity.
 * Provides fluent API for creating test Product instances.
 */
public class ProductTestDataBuilder {

    private static final AtomicLong ID_COUNTER = new AtomicLong(1);

    private Long id;
    private String uuid;
    private String uid;
    private ProductType productType;
    private String sku;
    private BigDecimal basePrice;
    private String currency;
    private ProductStatus status;
    private Boolean isVisible;
    private ResponsiveMediaSet responsiveMediaSet;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;

    private ProductTestDataBuilder() {
        // Set defaults
        long currentId = ID_COUNTER.getAndIncrement();
        this.id = currentId;
        this.uuid = UUID.randomUUID().toString();
        this.uid = "product_" + currentId;
        this.sku = "SKU-" + String.format("%05d", currentId);
        this.basePrice = BigDecimal.valueOf(99.99);
        this.currency = "TRY";
        this.status = ProductStatus.DRAFT;
        this.isVisible = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.createdBy = 1L;
        this.updatedBy = 1L;
    }

    public static ProductTestDataBuilder aProduct() {
        return new ProductTestDataBuilder();
    }

    public static ProductTestDataBuilder aPublishedProduct() {
        return new ProductTestDataBuilder()
                .withStatus(ProductStatus.PUBLISHED);
    }

    public static ProductTestDataBuilder aDraftProduct() {
        return new ProductTestDataBuilder()
                .withStatus(ProductStatus.DRAFT);
    }

    public static ProductTestDataBuilder aHiddenProduct() {
        return new ProductTestDataBuilder()
                .withIsVisible(false);
    }

    public ProductTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ProductTestDataBuilder withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public ProductTestDataBuilder withUid(String uid) {
        this.uid = uid;
        return this;
    }

    public ProductTestDataBuilder withProductType(ProductType productType) {
        this.productType = productType;
        return this;
    }

    public ProductTestDataBuilder withSku(String sku) {
        this.sku = sku;
        return this;
    }

    public ProductTestDataBuilder withBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
        return this;
    }

    public ProductTestDataBuilder withBasePrice(double basePrice) {
        this.basePrice = BigDecimal.valueOf(basePrice);
        return this;
    }

    public ProductTestDataBuilder withCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public ProductTestDataBuilder withStatus(ProductStatus status) {
        this.status = status;
        return this;
    }

    public ProductTestDataBuilder withIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
        return this;
    }

    public ProductTestDataBuilder withResponsiveMediaSet(ResponsiveMediaSet responsiveMediaSet) {
        this.responsiveMediaSet = responsiveMediaSet;
        return this;
    }

    public ProductTestDataBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ProductTestDataBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public ProductTestDataBuilder withCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public ProductTestDataBuilder withUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
        return this;
    }

    public Product build() {
        Product product = new Product();
        product.setId(id);
        product.setUuid(uuid);
        product.setUid(uid);
        product.setProductType(productType);
        product.setSku(sku);
        product.setBasePrice(basePrice);
        product.setCurrency(currency);
        product.setStatus(status);
        product.setIsVisible(isVisible);
        product.setResponsiveMediaSet(responsiveMediaSet);
        product.setCreatedAt(createdAt);
        product.setUpdatedAt(updatedAt);
        product.setCreatedBy(createdBy);
        product.setUpdatedBy(updatedBy);
        return product;
    }

    /**
     * Resets the ID counter for test isolation.
     */
    public static void resetIdCounter() {
        ID_COUNTER.set(1);
    }
}
