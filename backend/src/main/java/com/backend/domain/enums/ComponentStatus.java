package com.backend.domain.enums;

public enum ComponentStatus {
  ACTIVE("active", "component.status.active", "Aktif", "Active"),
  INACTIVE("inactive", "component.status.inactive", "Pasif", "Inactive");

  private final String code;
  private final String messageKey;
  private final String displayNameTr;
  private final String displayNameEn;

  ComponentStatus(String code, String messageKey, String displayNameTr, String displayNameEn) {
    this.code = code;
    this.messageKey = messageKey;
    this.displayNameTr = displayNameTr;
    this.displayNameEn = displayNameEn;
  }

  public String getCode() {
    return code;
  }

  public String getMessageKey() {
    return messageKey;
  }

  public String getDisplayName(Language language) {
    return switch (language) {
      case TR -> displayNameTr;
      case EN -> displayNameEn;
      default -> displayNameTr;
    };
  }
}
