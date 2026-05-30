package com.backend.domain.enums;

/**
 * Enum representing the type of activity action performed on an entity.
 * Used for tracking site activity history.
 */
public enum ActivityAction {
    CREATED,
    UPDATED,
    DELETED,
    PUBLISHED,
    UNPUBLISHED,
    DRAFT_SAVED,
    DRAFT_DISCARDED,
    UPLOADED,
    ACTIVATED,
    DEACTIVATED
}
