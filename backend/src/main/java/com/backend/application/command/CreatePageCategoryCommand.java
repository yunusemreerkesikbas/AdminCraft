package com.backend.application.command;

public record CreatePageCategoryCommand(
    String uid,
    Long parentId,
    Boolean active,
    String styleClasses,
    Integer sortOrder
) {
}
