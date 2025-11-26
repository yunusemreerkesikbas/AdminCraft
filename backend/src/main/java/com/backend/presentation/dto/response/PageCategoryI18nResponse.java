package com.backend.presentation.dto.response;

import com.backend.domain.enums.Language;
import java.time.LocalDateTime;

public record PageCategoryI18nResponse(
    Long id,
    String uuid,
    String uid,
    Language language,
    String url,
    String title,
    String metaTitle,
    String metaDescription,
    Boolean active,
    LocalDateTime updatedAt,
    Boolean fallbackLanguage) {
}
