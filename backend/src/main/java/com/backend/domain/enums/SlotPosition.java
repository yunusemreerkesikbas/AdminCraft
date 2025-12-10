package com.backend.domain.enums;

import lombok.Getter;

@Getter
public enum SlotPosition {
  TOP("TOP", "Top"),
  CENTER("CENTER", "Center"),
  BOTTOM("BOTTOM", "Bottom"),
  LEFT("LEFT", "Left"),
  RIGHT("RIGHT", "Right");

  private final String code;
  private final String displayName;

  SlotPosition(String code, String displayName) {
    this.code = code;
    this.displayName = displayName;
  }

  public static SlotPosition fromCode(String code) {
    for (SlotPosition position : values()) {
      if (position.code.equalsIgnoreCase(code)) {
        return position;
      }
    }
    return CENTER; // Default fallback
  }
}
