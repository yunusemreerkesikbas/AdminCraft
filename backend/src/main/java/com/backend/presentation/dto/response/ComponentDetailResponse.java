package com.backend.presentation.dto.response;

import com.backend.domain.entity.Component;
import com.backend.domain.enums.ComponentStatus;

import java.util.List;

public record ComponentDetailResponse(
        Long id,
        String code,
        String name,
        Long componentTypeId,
        String componentTypeName,
        ComponentStatus status,
        List<EntryResponse> entries) {
    public static ComponentDetailResponse from(
            Component component,
            String componentTypeName,
            List<EntryResponse> entries) {
        return new ComponentDetailResponse(
                component.getId(),
                component.getCode(),
                component.getName(),
                component.getComponentTypeId(),
                componentTypeName,
                component.getStatus(),
                entries);
    }
}
