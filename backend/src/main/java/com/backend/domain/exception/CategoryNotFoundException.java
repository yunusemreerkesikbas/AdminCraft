package com.backend.domain.exception;

/**
 * Exception thrown when a requested category is not found.
 * This is a domain-specific exception for category-related operations.
 */
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(Long categoryId) {
        super("Category not found with id: " + categoryId);
    }

    public CategoryNotFoundException(Long categoryId, Long tenantId) {
        super(String.format("Category not found with id %d for tenant %d", categoryId, tenantId));
    }

    public CategoryNotFoundException(String message) {
        super(message);
    }

    public CategoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}