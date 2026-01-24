package com.backend.testutil.builders;

import com.backend.domain.entity.ProductAttributeDefinition;
import com.backend.domain.entity.ProductType;
import com.backend.domain.enums.ProductFieldType;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test data builder for ProductAttributeDefinition entity.
 * Provides fluent API for creating test ProductAttributeDefinition instances.
 */
public class ProductAttributeDefinitionTestDataBuilder {

    private static final AtomicLong ID_COUNTER = new AtomicLong(1);

    private Long id;
    private String uuid;
    private String uid;
    private ProductType productType;
    private String code;
    private String name;
    private ProductFieldType fieldType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ProductAttributeDefinitionTestDataBuilder() {
        // Set defaults
        long currentId = ID_COUNTER.getAndIncrement();
        this.id = currentId;
        this.uuid = UUID.randomUUID().toString();
        this.uid = "attr_def_" + currentId;
        this.code = "attribute_" + currentId;
        this.name = "Attribute " + currentId;
        this.fieldType = ProductFieldType.TEXT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static ProductAttributeDefinitionTestDataBuilder anAttributeDefinition() {
        return new ProductAttributeDefinitionTestDataBuilder();
    }

    public static ProductAttributeDefinitionTestDataBuilder aTextAttribute() {
        return new ProductAttributeDefinitionTestDataBuilder()
                .withFieldType(ProductFieldType.TEXT);
    }

    public static ProductAttributeDefinitionTestDataBuilder aNumberAttribute() {
        return new ProductAttributeDefinitionTestDataBuilder()
                .withFieldType(ProductFieldType.NUMBER);
    }

    public static ProductAttributeDefinitionTestDataBuilder aBooleanAttribute() {
        return new ProductAttributeDefinitionTestDataBuilder()
                .withFieldType(ProductFieldType.BOOLEAN);
    }


    public ProductAttributeDefinitionTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ProductAttributeDefinitionTestDataBuilder withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public ProductAttributeDefinitionTestDataBuilder withUid(String uid) {
        this.uid = uid;
        return this;
    }

    public ProductAttributeDefinitionTestDataBuilder withProductType(ProductType productType) {
        this.productType = productType;
        return this;
    }

    public ProductAttributeDefinitionTestDataBuilder withCode(String code) {
        this.code = code;
        return this;
    }

    public ProductAttributeDefinitionTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ProductAttributeDefinitionTestDataBuilder withFieldType(ProductFieldType fieldType) {
        this.fieldType = fieldType;
        return this;
    }

    public ProductAttributeDefinitionTestDataBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ProductAttributeDefinitionTestDataBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public ProductAttributeDefinition build() {
        ProductAttributeDefinition definition = new ProductAttributeDefinition();
        definition.setId(id);
        definition.setUuid(uuid);
        definition.setUid(uid);
        definition.setProductType(productType);
        definition.setCode(code);
        definition.setName(name);
        definition.setFieldType(fieldType);
        definition.setCreatedAt(createdAt);
        definition.setUpdatedAt(updatedAt);
        return definition;
    }

    /**
     * Resets the ID counter for test isolation.
     */
    public static void resetIdCounter() {
        ID_COUNTER.set(1);
    }
}
