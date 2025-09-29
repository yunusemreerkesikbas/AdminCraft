package com.backend.presentation.dto.response;

import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.ComponentType;

import java.util.List;
import java.util.Map;

public record ComponentResponse(
    Long id,
    Long tenantId,
    ComponentType type,
    String key,
    String uid,
    String uuid,
    List<NavbarItemEntryResponse> items) {
}
