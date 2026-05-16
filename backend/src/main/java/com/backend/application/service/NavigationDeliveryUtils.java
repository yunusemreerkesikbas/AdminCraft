package com.backend.application.service;

import com.backend.domain.enums.NavigationItemType;

final class NavigationDeliveryUtils {
  private NavigationDeliveryUtils() {}

  private static final String HTTP_SCHEME = "http:";
  private static final String HTTPS_SCHEME = "https:";
  private static final String MAILTO_SCHEME = "mailto:";
  private static final String TEL_SCHEME = "tel:";

  static String resolveLocalizedHref(String href, boolean isExternal, String langCode) {
    if (href == null || href.isBlank()) return null;
    String h = href.trim();
    if (!isSafeHref(h)) return null;
    if (hasAllowedAbsoluteScheme(h)) return h;
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

  private static boolean isSafeHref(String href) {
    if (containsControlCharacter(href)) {
      return false;
    }

    if (href.startsWith("#")) {
      return true;
    }

    if (href.startsWith("/")) {
      return !href.startsWith("//");
    }

    if (href.startsWith("?")) {
      return true;
    }

    int colonIndex = href.indexOf(':');
    if (colonIndex < 0) {
      return true;
    }
    if (colonIndex == 0) {
      return false;
    }

    return hasAllowedAbsoluteScheme(href);
  }

  private static boolean hasAllowedAbsoluteScheme(String href) {
    String lower = href.toLowerCase(java.util.Locale.ROOT);
    return lower.startsWith(HTTP_SCHEME)
        || lower.startsWith(HTTPS_SCHEME)
        || lower.startsWith(MAILTO_SCHEME)
        || lower.startsWith(TEL_SCHEME);
  }

  private static boolean containsControlCharacter(String href) {
    for (int i = 0; i < href.length(); i++) {
      char ch = href.charAt(i);
      if (ch <= 0x1F || ch == 0x7F) {
        return true;
      }
    }
    return false;
  }
}
