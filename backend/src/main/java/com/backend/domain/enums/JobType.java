package com.backend.domain.enums;

public enum JobType {

    LANGUAGE_PROVISIONING("Language Provisioning", "Provisions empty i18n containers for new languages");

    private final String displayName;
    private final String description;

    JobType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
