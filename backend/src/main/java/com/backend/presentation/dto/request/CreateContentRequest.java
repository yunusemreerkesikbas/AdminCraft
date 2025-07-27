package com.backend.presentation.dto.request;

import com.backend.domain.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateContentRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title,
    
    @NotBlank(message = "Slug is required")
    @Size(max = 255, message = "Slug must not exceed 255 characters")
    String slug,
    
    String excerpt,
    
    @NotBlank(message = "Content data is required")
    String data,
    
    @NotNull(message = "Language is required")
    Language language,
    
    Long parentContentId,
    
    @NotNull(message = "Content type ID is required")
    Long contentTypeId,
    
    String metaTitle,
    String metaDescription,
    String metaKeywords,
    
    Boolean isFeatured,
    Boolean isSticky,
    Boolean requiresLogin,
    String password
) {
    public CreateContentRequest {
        if (title != null && title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (slug != null && slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Slug cannot be empty");
        }
        if (data != null && data.trim().isEmpty()) {
            throw new IllegalArgumentException("Content data cannot be empty");
        }
    }
}