package com.backend.testutil.builders;

import com.backend.domain.entity.Product;
import com.backend.domain.entity.ProductI18n;
import com.backend.domain.enums.Language;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test data builder for ProductI18n entity.
 * Provides fluent API for creating test ProductI18n instances.
 */
public class ProductI18nTestDataBuilder {

    private static final AtomicLong ID_COUNTER = new AtomicLong(1);

    private Long id;
    private String uuid;
    private String uid;
    private Product product;
    private Language language;
    private String name;
    private String shortDescription;
    private String description;
    private String seoTitle;
    private String seoDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;

    private ProductI18nTestDataBuilder() {
        // Set defaults
        long currentId = ID_COUNTER.getAndIncrement();
        this.id = currentId;
        this.uuid = UUID.randomUUID().toString();
        this.uid = "product_i18n_" + currentId;
        this.language = Language.TR;
        this.name = "Test Product " + currentId;
        this.shortDescription = "Short description for product " + currentId;
        this.description = "<p>Description for product " + currentId + "</p>";
        this.seoTitle = "SEO Title " + currentId;
        this.seoDescription = "SEO Description for product " + currentId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.createdBy = 1L;
        this.updatedBy = 1L;
    }

    public static ProductI18nTestDataBuilder aProductI18n() {
        return new ProductI18nTestDataBuilder();
    }

    public static ProductI18nTestDataBuilder aProductI18nInTurkish() {
        return new ProductI18nTestDataBuilder()
                .withLanguage(Language.TR)
                .withName("Türkçe Ürün");
    }

    public static ProductI18nTestDataBuilder aProductI18nInEnglish() {
        return new ProductI18nTestDataBuilder()
                .withLanguage(Language.EN)
                .withName("English Product");
    }

    public static ProductI18nTestDataBuilder aProductI18nWithoutName() {
        return new ProductI18nTestDataBuilder()
                .withName(null);
    }

    public static ProductI18nTestDataBuilder aProductI18nWithEmptyName() {
        return new ProductI18nTestDataBuilder()
                .withName("");
    }

    public ProductI18nTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ProductI18nTestDataBuilder withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public ProductI18nTestDataBuilder withUid(String uid) {
        this.uid = uid;
        return this;
    }

    public ProductI18nTestDataBuilder withProduct(Product product) {
        this.product = product;
        return this;
    }

    public ProductI18nTestDataBuilder withLanguage(Language language) {
        this.language = language;
        return this;
    }

    public ProductI18nTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ProductI18nTestDataBuilder withShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
        return this;
    }

    public ProductI18nTestDataBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public ProductI18nTestDataBuilder withSeoTitle(String seoTitle) {
        this.seoTitle = seoTitle;
        return this;
    }

    public ProductI18nTestDataBuilder withSeoDescription(String seoDescription) {
        this.seoDescription = seoDescription;
        return this;
    }

    public ProductI18nTestDataBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ProductI18nTestDataBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public ProductI18nTestDataBuilder withCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public ProductI18nTestDataBuilder withUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
        return this;
    }

    public ProductI18n build() {
        ProductI18n i18n = new ProductI18n();
        i18n.setId(id);
        i18n.setUuid(uuid);
        i18n.setUid(uid);
        i18n.setProduct(product);
        i18n.setLanguage(language);
        i18n.setName(name);
        i18n.setShortDescription(shortDescription);
        i18n.setDescription(description);
        i18n.setSeoTitle(seoTitle);
        i18n.setSeoDescription(seoDescription);
        i18n.setCreatedAt(createdAt);
        i18n.setUpdatedAt(updatedAt);
        i18n.setCreatedBy(createdBy);
        i18n.setUpdatedBy(updatedBy);
        return i18n;
    }

    /**
     * Resets the ID counter for test isolation.
     */
    public static void resetIdCounter() {
        ID_COUNTER.set(1);
    }
}
