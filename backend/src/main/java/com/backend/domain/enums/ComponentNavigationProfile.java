package com.backend.domain.enums;

public enum ComponentNavigationProfile {
    NONE,
    NODE,
    NODE_REQUIRED,
    NODE_WITH_LINK,
    NODE_WITH_TYPE,
    CATEGORY;

    public boolean supportsNavigationNode() {
        return this != NONE;
    }

    public boolean requiresNavigationNode() {
        return this == NODE_REQUIRED || this == CATEGORY;
    }

    public boolean supportsNavigationLinkNode() {
        return this == NODE_WITH_LINK;
    }

    public boolean supportsNavigationType() {
        return this == NODE_WITH_TYPE || this == CATEGORY;
    }

    public boolean supportsSearchBox() {
        return this == CATEGORY;
    }
}
