package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePageCategoryRequest(
        @NotNull Long id,
        @NotNull Long tenantId,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 150) @Pattern(regexp = "[a-z0-9-]+", message = "validation.slug.pattern") String slug,
        Long parentId) {
    public UpdatePageCategoryRequest {
        if (name != null)
            name = name.trim();
        if (slug != null)
            slug = slug.toLowerCase();
    }
}
