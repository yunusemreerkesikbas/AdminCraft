package com.backend.application.service;

import com.backend.domain.enums.NavigationItemType;

final class NavigationDeliveryUtils {
  private NavigationDeliveryUtils() {}

  static String resolveLocalizedHref(String href, boolean isExternal, String langCode) {
    if (href == null || href.isBlank()) return null;
    String h = href.trim();
    if (langCode == null || langCode.isBlank()) return h;
    if (h.startsWith("#")) return h;
    if (isExternal) return h;
    if (h.equals("/")) return "/" + langCode;
    if (h.startsWith("/" + langCode + "/") || h.equals("/" + langCode)) return h;
    return "/" + langCode + (h.startsWith("/") ? h : "/" + h);
  }

  static String resolveEntryHref(NavigationItemType itemType, String url, String itemId,
      boolean isExternal, String langCode) {
    if (itemType == NavigationItemType.URL && url != null) {
      return resolveLocalizedHref(url, isExternal, langCode);
    }
    if (itemType == NavigationItemType.PAGE && itemId != null) {
      return "/" + langCode + (itemId.startsWith("/") ? itemId : "/" + itemId);
    }
    return null;
  }
}
