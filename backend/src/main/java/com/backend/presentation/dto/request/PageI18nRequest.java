package com.backend.presentation.dto.request;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record PageI18nRequest(

        @NotNull(message = "validation.language.required") Language language,

        @Size(max = 255, message = "validation.url.path.size") String urlPath,

        @Size(max = 200, message = "validation.title.size") String title,

        @Size(max = 200, message = "validation.subtitle.size") String subtitle,

        @Size(max = 60, message = "validation.meta.title.size") String metaTitle,

        @Size(max = 160, message = "validation.meta.description.size") String metaDescription,

        String description, // LONGTEXT - no size limit

        String descriptionHtml, // LONGTEXT - no size limit

        PageStatus status, // defaults to DRAFT in entity if null

        LocalDateTime scheduledAt // for scheduling future publication
) {
    public PageI18nRequest {
        // Normalize URL path: trim and convert to lowercase
        if (urlPath != null && !urlPath.trim().isEmpty()) {
            urlPath = urlPath.trim().toLowerCase();
        }

        // Trim text fields
        if (title != null) {
            title = title.trim();
        }
        if (subtitle != null) {
            subtitle = subtitle.trim();
        }
        if (metaTitle != null) {
            metaTitle = metaTitle.trim();
        }
        if (metaDescription != null) {
            metaDescription = metaDescription.trim();
        }
    }
}
