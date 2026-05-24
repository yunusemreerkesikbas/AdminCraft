package com.backend.application.cms.preview;

import java.util.Map;

public record ComponentEntryI18nDraftPayload(
    String title,
    boolean titlePresent,
    String description,
    boolean descriptionPresent,
    Map<String, Object> dynamicFields,
    boolean dynamicFieldsPresent) {
}
