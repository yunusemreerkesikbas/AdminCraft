package com.backend.application.cms.preview;

public record ComponentEntryDraftPayload(
    Integer sortOrder,
    Boolean isVisible,
    String styleClasses,
    Long responsiveMediaId) {
}
