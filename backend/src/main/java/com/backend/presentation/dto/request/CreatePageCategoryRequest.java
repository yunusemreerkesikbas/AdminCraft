package com.backend.presentation.dto.request;

import jakarta.validation.constraints.*;

public record CreatePageCategoryRequest(
        @NotNull(message = "Tenant ID is required") 
        @Min(value = 1, message = "Tenant ID must be positive") 
        Long tenantId,
        
        @NotBlank(message = "Category name is required") 
        @Size(min = 1, max = 100, message = "Category name must be 1-100 characters") 
        @Pattern(regexp = "^[a-zA-ZçğıöşüÇĞIİÖŞÜ0-9\\s\\-_.,]+$", 
                message = "Category name contains invalid characters") 
        String name,
        
        @NotBlank(message = "Category slug is required") 
        @Size(min = 1, max = 150, message = "Slug must be 1-150 characters") 
        @Pattern(regexp = "^[a-z0-9-]+$", 
                message = "Slug can only contain lowercase letters, numbers and dashes") 
        String slug,
        
        @Min(value = 1, message = "Parent ID must be positive") 
        Long parentId,
        
        @Min(value = 0, message = "Sort order cannot be negative") 
        @Max(value = 9999, message = "Sort order cannot exceed 9999") 
        Integer sortOrder) {
    
    public CreatePageCategoryRequest {
        // GÜVENLIK: Input sanitization ve validation
        if (name != null) {
            name = name.trim();
            // XSS prevention için HTML tag'leri temizle
            if (name.contains("<") || name.contains(">")) {
                throw new IllegalArgumentException("Category name cannot contain HTML tags");
            }
        }
        
        if (slug != null) {
            slug = slug.toLowerCase().trim();
            // Path traversal prevention
            if (slug.contains("..") || slug.contains("./") || slug.contains("\\")) {
                throw new IllegalArgumentException("Invalid slug format - security violation");
            }
        }
        
        // Business validation
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }
}
