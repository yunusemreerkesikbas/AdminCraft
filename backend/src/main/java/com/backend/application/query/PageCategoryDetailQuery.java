package com.backend.application.query;

public record PageCategoryDetailQuery(
    Long id,
    Long tenantId,
    boolean includeTranslations
) {
}
