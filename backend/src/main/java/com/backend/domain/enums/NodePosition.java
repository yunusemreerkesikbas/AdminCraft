package com.backend.domain.enums;

import lombok.Getter;

@Getter
public enum NodePosition {
  TOP("TOP", "Top"),
  CENTER("CENTER", "Center"),
  BOTTOM("BOTTOM", "Bottom"),
  LEFT("LEFT", "Left"),
  RIGHT("RIGHT", "Right");

  private final String code;
  private final String displayName;

  NodePosition(String code, String displayName) {
    this.code = code;
    this.displayName = displayName;
  }

  public static NodePosition fromCode(String code) {
    for (NodePosition position : values()) {
      if (position.code.equalsIgnoreCase(code)) {
        return position;
      }
    }
    return LEFT; // Default fallback
  }
}
