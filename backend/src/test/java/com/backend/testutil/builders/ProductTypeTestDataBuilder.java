package com.backend.testutil.builders;

import com.backend.domain.entity.ProductType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test data builder for ProductType entity.
 * Provides fluent API for creating test ProductType instances.
 */
public class ProductTypeTestDataBuilder {

    private static final AtomicLong ID_COUNTER = new AtomicLong(1);

    private Long id;
    private String uuid;
    private String uid;
    private String code;
    private String name;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;

    private ProductTypeTestDataBuilder() {
        // Set defaults
        long currentId = ID_COUNTER.getAndIncrement();
        this.id = currentId;
        this.uuid = UUID.randomUUID().toString();
        this.uid = "product_type_" + currentId;
        this.code = "product_type_" + currentId;
        this.name = "Product Type " + currentId;
        this.category = "general";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.createdBy = 1L;
        this.updatedBy = 1L;
    }

    public static ProductTypeTestDataBuilder aProductType() {
        return new ProductTypeTestDataBuilder();
    }

    public static ProductTypeTestDataBuilder aProductTypeWithCode(String code) {
        return new ProductTypeTestDataBuilder()
                .withCode(code)
                .withName(code.replace("_", " ").toUpperCase());
    }

    public ProductTypeTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ProductTypeTestDataBuilder withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public ProductTypeTestDataBuilder withUid(String uid) {
        this.uid = uid;
        return this;
    }

    public ProductTypeTestDataBuilder withCode(String code) {
        this.code = code;
        return this;
    }

    public ProductTypeTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ProductTypeTestDataBuilder withCategory(String category) {
        this.category = category;
        return this;
    }

    public ProductTypeTestDataBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ProductTypeTestDataBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public ProductTypeTestDataBuilder withCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public ProductTypeTestDataBuilder withUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
        return this;
    }

    public ProductType build() {
        ProductType productType = new ProductType();
        productType.setId(id);
        productType.setUuid(uuid);
        productType.setUid(uid);
        productType.setCode(code);
        productType.setName(name);
        productType.setCategory(category);
        productType.setCreatedAt(createdAt);
        productType.setUpdatedAt(updatedAt);
        productType.setCreatedBy(createdBy);
        productType.setUpdatedBy(updatedBy);
        return productType;
    }

    /**
     * Resets the ID counter for test isolation.
     */
    public static void resetIdCounter() {
        ID_COUNTER.set(1);
    }
}
