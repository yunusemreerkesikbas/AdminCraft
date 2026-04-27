package com.backend.application.dto.response;

import java.util.List;

public record MediaLinkedComponentUsageResponse(
    Long componentId,
    String componentUid,
    String componentName,
    String componentLabel,
    String componentTypeName,
    Long entryId,
    String entryUid,
    Integer entrySortOrder,
    String entryTitle,
    String entryLabel,
    List<MediaLinkedComponentUsageLinkTypeResponse> linkTypes,
    Long responsiveSetId) {
}
