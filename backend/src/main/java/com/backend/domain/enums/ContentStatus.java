package com.backend.domain.enums;

import lombok.Getter;

@Getter
public enum ContentStatus {
    DRAFT("DRAFT", "content.status.draft", "Taslak", "Draft"),
    PUBLISHED("PUBLISHED", "content.status.published", "Yayınlandı", "Published"),
    ARCHIVED("ARCHIVED", "content.status.archived", "Arşivlendi", "Archived"),
    SCHEDULED("SCHEDULED", "content.status.scheduled", "Zamanlandı", "Scheduled"),
    REVIEWING("REVIEWING", "content.status.reviewing", "İnceleme", "Under Review");

    private final String code;
    private final String messageKey;
    private final String displayNameTr;
    private final String displayNameEn;

    ContentStatus(String code, String messageKey, String displayNameTr, String displayNameEn) {
        this.code = code;
        this.messageKey = messageKey;
        this.displayNameTr = displayNameTr;
        this.displayNameEn = displayNameEn;
    }

    public String getDisplayName(Language language) {
        return switch (language) {
            case TR -> displayNameTr;
            case EN -> displayNameEn;
            default -> displayNameTr; // fallback to Turkish
        };
    }

    public boolean canTransitionTo(ContentStatus newStatus) {
        return switch (this) {
            case DRAFT -> switch (newStatus) {
                case PUBLISHED, REVIEWING, ARCHIVED -> true;
                default -> false;
            };
            case REVIEWING -> switch (newStatus) {
                case DRAFT, PUBLISHED, ARCHIVED -> true;
                default -> false;
            };
            case PUBLISHED -> switch (newStatus) {
                case DRAFT, ARCHIVED -> true;
                default -> false;
            };
            case SCHEDULED -> switch (newStatus) {
                case DRAFT, PUBLISHED, ARCHIVED -> true;
                default -> false;
            };
            case ARCHIVED -> switch (newStatus) {
                case DRAFT -> true;
                default -> false;
            };
        };
    }

    public boolean isPublic() {
        return this == PUBLISHED;
    }

    public boolean canBeEdited() {
        return this == DRAFT || this == REVIEWING;
    }
}