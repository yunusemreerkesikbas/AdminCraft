package com.backend.domain.enums;

import java.util.Arrays;
import java.util.Optional;

public enum NavigationType {
  MAINMENU,
  STATICPAGE;

  public static Optional<NavigationType> fromCode(String value) {
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }
    String normalized = value.trim();
    return Arrays.stream(values())
        .filter(type -> type.name().equalsIgnoreCase(normalized))
        .findFirst();
  }
}
