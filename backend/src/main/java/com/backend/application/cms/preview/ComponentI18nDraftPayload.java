package com.backend.application.cms.preview;

public record ComponentI18nDraftPayload(
    String title,
    boolean titlePresent,
    String subtitle,
    boolean subtitlePresent,
    String description,
    boolean descriptionPresent) {
}
