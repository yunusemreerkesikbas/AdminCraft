package com.backend.application.command;

public record UpdatePageCategoryCommand(
    Long parentId,
    Boolean active,
    String styleClasses,
    Integer sortOrder
) {
}
