package com.backend.domain.enums;

import lombok.Getter;

@Getter
public enum NavigationItemType {
  URL("URL", "External/Internal URL"),
  PAGE("PAGE", "CMS Page"),
  COMPONENT("COMPONENT", "CMS Component");

  private final String code;
  private final String displayName;

  NavigationItemType(String code, String displayName) {
    this.code = code;
    this.displayName = displayName;
  }

  public static NavigationItemType fromCode(String code) {
    for (NavigationItemType type : values()) {
      if (type.code.equalsIgnoreCase(code)) {
        return type;
      }
    }
    return URL; // Default fallback
  }

  public boolean requiresUrl() {
    return this == URL;
  }

  public boolean requiresItemId() {
    return this == PAGE || this == COMPONENT;
  }
}
