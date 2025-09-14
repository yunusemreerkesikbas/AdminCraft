package com.backend.presentation.dto.request;

import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.ComponentType;

public record ComponentListFilter(
    Long tenantId,
    ComponentType type,
    ComponentStatus status) {
}
