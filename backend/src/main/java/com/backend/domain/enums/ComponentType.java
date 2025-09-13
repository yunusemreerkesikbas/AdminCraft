package com.backend.domain.enums;

public enum ComponentType {
  NAVBAR("navbar", "ui.component.type.navbar", "Navigasyon", "Navbar"),
  LOGO("logo", "ui.component.type.logo", "Logo", "Logo"),
  CTA("cta", "ui.component.type.cta", "CTA", "CTA"),
  BRANDS("brands", "ui.component.type.brands", "Markalar", "Brands"),
  FAQ("faq", "ui.component.type.faq", "SSS", "FAQ"),
  BREADCRUMB("breadcrumb", "ui.component.type.breadcrumb", "İçerik Yolu", "Breadcrumb");

  private final String code;
  private final String messageKey;
  private final String displayNameTr;
  private final String displayNameEn;

  ComponentType(String code, String messageKey, String displayNameTr, String displayNameEn) {
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

  public static ComponentType fromCode(String code) {
    if (code == null) {
      throw new IllegalArgumentException("ComponentType code is null");
    }
    for (ComponentType t : values()) {
      if (t.code.equalsIgnoreCase(code.trim())) {
        return t;
      }
    }
    throw new IllegalArgumentException("Unsupported ComponentType code: " + code);
  }
}
