package com.backend.testutil.builders;

import com.backend.domain.entity.Category;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test data builder for Category entity.
 * Provides fluent API for creating test Category instances.
 */
public class CategoryTestDataBuilder {

    private static final AtomicLong ID_COUNTER = new AtomicLong(1);

    private Long id;
    private String uuid;
    private String uid;
    private String code;
    private Long parentId;
    private Integer sortOrder;
    private Boolean isVisible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;

    private CategoryTestDataBuilder() {
        // Set defaults
        long currentId = ID_COUNTER.getAndIncrement();
        this.id = currentId;
        this.uuid = UUID.randomUUID().toString();
        this.uid = "category_" + currentId;
        this.code = "category_" + currentId;
        this.parentId = null; // Root category by default
        this.sortOrder = 0;
        this.isVisible = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.createdBy = 1L;
        this.updatedBy = 1L;
    }

    public static CategoryTestDataBuilder aCategory() {
        return new CategoryTestDataBuilder();
    }

    public static CategoryTestDataBuilder aRootCategory() {
        return new CategoryTestDataBuilder()
                .withParentId(null);
    }

    public static CategoryTestDataBuilder aChildCategory(Long parentId) {
        return new CategoryTestDataBuilder()
                .withParentId(parentId);
    }

    public static CategoryTestDataBuilder aHiddenCategory() {
        return new CategoryTestDataBuilder()
                .withIsVisible(false);
    }

    public static CategoryTestDataBuilder aCategoryWithCode(String code) {
        return new CategoryTestDataBuilder()
                .withCode(code);
    }

    public CategoryTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public CategoryTestDataBuilder withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public CategoryTestDataBuilder withUid(String uid) {
        this.uid = uid;
        return this;
    }

    public CategoryTestDataBuilder withCode(String code) {
        this.code = code;
        return this;
    }

    public CategoryTestDataBuilder withParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public CategoryTestDataBuilder withSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    public CategoryTestDataBuilder withIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
        return this;
    }

    public CategoryTestDataBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public CategoryTestDataBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public CategoryTestDataBuilder withCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public CategoryTestDataBuilder withUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
        return this;
    }

    public Category build() {
        Category category = new Category();
        category.setId(id);
        category.setUuid(uuid);
        category.setUid(uid);
        category.setCode(code);
        category.setParentId(parentId);
        category.setSortOrder(sortOrder);
        category.setIsVisible(isVisible);
        category.setCreatedAt(createdAt);
        category.setUpdatedAt(updatedAt);
        category.setCreatedBy(createdBy);
        category.setUpdatedBy(updatedBy);
        return category;
    }

    /**
     * Resets the ID counter for test isolation.
     */
    public static void resetIdCounter() {
        ID_COUNTER.set(1);
    }
}
