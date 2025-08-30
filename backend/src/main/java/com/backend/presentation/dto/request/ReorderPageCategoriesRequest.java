package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReorderPageCategoriesRequest(
    @NotNull Long tenantId,
    Long parentId,
    @NotEmpty List<Long> orderedIds) {
}
