package com.backend.testutil.builders;

import com.backend.domain.entity.Category;
import com.backend.domain.entity.CategoryI18n;
import com.backend.domain.enums.Language;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test data builder for CategoryI18n entity.
 * Provides fluent API for creating test CategoryI18n instances.
 */
public class CategoryI18nTestDataBuilder {

    private static final AtomicLong ID_COUNTER = new AtomicLong(1);

    private Long id;
    private String uuid;
    private String uid;
    private Category category;
    private Language language;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;

    private CategoryI18nTestDataBuilder() {
        // Set defaults
        long currentId = ID_COUNTER.getAndIncrement();
        this.id = currentId;
        this.uuid = UUID.randomUUID().toString();
        this.uid = "category_i18n_" + currentId;
        this.language = Language.TR;
        this.name = "Test Category " + currentId;
        this.description = "<p>Description for category " + currentId + "</p>";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.createdBy = 1L;
        this.updatedBy = 1L;
    }

    public static CategoryI18nTestDataBuilder aCategoryI18n() {
        return new CategoryI18nTestDataBuilder();
    }

    public static CategoryI18nTestDataBuilder aCategoryI18nInTurkish() {
        return new CategoryI18nTestDataBuilder()
                .withLanguage(Language.TR)
                .withName("Türkçe Kategori");
    }

    public static CategoryI18nTestDataBuilder aCategoryI18nInEnglish() {
        return new CategoryI18nTestDataBuilder()
                .withLanguage(Language.EN)
                .withName("English Category");
    }

    public CategoryI18nTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public CategoryI18nTestDataBuilder withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public CategoryI18nTestDataBuilder withUid(String uid) {
        this.uid = uid;
        return this;
    }

    public CategoryI18nTestDataBuilder withCategory(Category category) {
        this.category = category;
        return this;
    }

    public CategoryI18nTestDataBuilder withLanguage(Language language) {
        this.language = language;
        return this;
    }

    public CategoryI18nTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CategoryI18nTestDataBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public CategoryI18nTestDataBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public CategoryI18nTestDataBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public CategoryI18nTestDataBuilder withCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public CategoryI18nTestDataBuilder withUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
        return this;
    }

    public CategoryI18n build() {
        CategoryI18n i18n = new CategoryI18n();
        i18n.setId(id);
        i18n.setUuid(uuid);
        i18n.setUid(uid);
        i18n.setCategory(category);
        i18n.setLanguage(language);
        i18n.setName(name);
        i18n.setDescription(description);
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
