package com.backend.application.cms.preview;

public record SmartEditDraftFieldChange(
    String field,
    String label,
    Object before,
    Object after,
    String valueType) {
}
