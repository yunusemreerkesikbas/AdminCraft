package com.backend.application.dto.response;

public record MediaLinkedComponentUsageResponse(
    Long componentId,
    String componentUid,
    String componentName,
    String componentTypeName,
    Long entryId,
    String entryUid,
    Integer entrySortOrder,
    String entryTitle,
    String linkType,
    Long responsiveSetId) {
}
