package com.backend.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Standard values for the "robots" meta tag.
 */
@Getter
@RequiredArgsConstructor
public enum RobotsMetaTag {

  INDEX_FOLLOW("index,follow"),
  NOINDEX_NOFOLLOW("noindex,nofollow"),
  INDEX_NOFOLLOW("index,nofollow"),
  NOINDEX_FOLLOW("noindex,follow");

  private final String value;

  @JsonValue
  @Override
  public String toString() {
    return value;
  }

  @JsonCreator
  public static RobotsMetaTag fromValue(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    // Normalize spaces: "index, follow" -> "index,follow"
    String normalized = value.replace(" ", "").toLowerCase();

    for (RobotsMetaTag type : values()) {
      if (type.value.equals(normalized)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown RobotsMetaTag value: " + value);
  }
}
