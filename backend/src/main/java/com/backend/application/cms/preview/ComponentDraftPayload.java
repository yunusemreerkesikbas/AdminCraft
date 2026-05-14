package com.backend.application.cms.preview;

import com.backend.domain.enums.NavigationType;

public record ComponentDraftPayload(
    String name,
    Integer displayOrder,
    Boolean isVisible,
    String styleClasses,
    Long responsiveMediaId,
    Long navigationNodeId,
    NavigationType navigationType,
    Boolean searchBox) {
}
