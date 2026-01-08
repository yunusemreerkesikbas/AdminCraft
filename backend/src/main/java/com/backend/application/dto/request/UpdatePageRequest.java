package com.backend.application.dto.request;

import com.backend.domain.enums.Language;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePageRequest(
        @NotNull Long id,
        @NotNull Long tenantId,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 200) @Pattern(regexp = "[a-z0-9-]+", message = "validation.slug.pattern") String slug,
        @NotNull Language language,
        @Size(max = 255) @Pattern(regexp = "https?://.+", message = "validation.url.invalid") String canonicalUrl,
        @Size(max = 255) String styleClasses,
        String description) {

    public UpdatePageRequest {
        if (title != null)
            title = title.trim();
        if (slug != null)
            slug = slug.toLowerCase();
    }
}
