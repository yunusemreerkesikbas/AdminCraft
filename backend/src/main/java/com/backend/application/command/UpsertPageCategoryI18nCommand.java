package com.backend.application.command;

public record UpsertPageCategoryI18nCommand(
    String url,
    String title,
    String metaTitle,
    String metaDescription,
    Boolean active
) {
}
