package com.backend.presentation.dto.response;

import java.util.List;
import java.util.Map;

public record NavbarItemResponse(
    Long id,
    String uid,
    String uuid,
    Long parentId,
    Integer level,
    boolean visible,
    Integer sortOrder,
    Map<String, I18n> translations,
    List<NavbarItemResponse> children) {
  public static record I18n(
      String title,
      String subtitle,
      String url,
      String seoTitle,
      String seoDescription,
      String seoKeywords) {
  }
}

