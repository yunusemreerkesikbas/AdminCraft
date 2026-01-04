package com.backend.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EntryFieldType {
  TEXT,
  TEXTAREA,
  NUMBER,
  BOOLEAN,
  MEDIA;

  @JsonValue
  public String toValue() {
    return name().toLowerCase();
  }

  @JsonCreator
  public static EntryFieldType fromValue(String value) {
    if (value == null) {
      return null;
    }
    return valueOf(value.toUpperCase());
  }
}
