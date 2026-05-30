package com.backend.application.cms.preview;

import java.util.List;

public record SmartEditDraftFieldChange(
    String field,
    String label,
    Object before,
    Object after,
    String valueType,
    String beforeText,
    String afterText,
    List<SmartEditMediaPreviewResponse> mediaBefore,
    List<SmartEditMediaPreviewResponse> mediaAfter,
    boolean isMedia) {

    public SmartEditDraftFieldChange(
        String field,
        String label,
        Object before,
        Object after,
        String valueType) {
        this(field, label, before, after, valueType, null, null, List.of(), List.of(), false);
    }
}
