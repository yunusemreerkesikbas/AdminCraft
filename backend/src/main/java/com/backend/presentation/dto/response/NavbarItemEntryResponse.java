package com.backend.presentation.dto.response;

import java.util.Map;

public record NavbarItemEntryResponse(
    Long id,
    String uid,
    String uuid,
    Long parentId,
    Integer level,
    boolean visible,
    Integer sortOrder,
    Map<String, NavbarItemResponse.I18n> translations) {
}


