package com.backend.application.dto.request;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.RobotTag;
import com.backend.presentation.validation.Slug;
import com.backend.presentation.validation.Uid;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import static com.backend.shared.constants.ValidationConstants.PAGE_CANONICAL_URL_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.PAGE_TITLE_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.UID_PAGE_MAX_LENGTH;

public record CreatePageRequest(
        @NotNull Long tenantId,
        @NotBlank
        @Size(max = PAGE_TITLE_MAX_LENGTH, message = "validation.page.title.size")
        String title,
        @Slug
        String slug,
        @NotNull Language language,
        @Size(max = PAGE_CANONICAL_URL_MAX_LENGTH, message = "validation.page.canonicalUrl.size")
        String canonicalUrl,
        RobotTag robotTag,
        @Uid(maxLength = UID_PAGE_MAX_LENGTH, required = false)
        String uid) {
    public CreatePageRequest {
        if (title != null)
            title = title.trim();
        if (slug != null)
            slug = slug.toLowerCase();
    }
}
