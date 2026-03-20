package com.backend.application.util;

import java.util.Map;

public final class UrlUtils {

  private UrlUtils() {}

  public static boolean isExternalUrl(Object value) {
    if (!(value instanceof String url)) return false;
    String trimmed = url.trim();
    return trimmed.startsWith("http://") || trimmed.startsWith("https://")
        || trimmed.startsWith("mailto:") || trimmed.startsWith("tel:");
  }

  public static boolean computeIsExternal(Map<String, Object> fields) {
    Object buttonUrl = fields.get("buttonUrl");
    Object linkUrl = fields.get("linkUrl");
    if (buttonUrl == null && linkUrl == null) return false;
    return isExternalUrl(buttonUrl) || isExternalUrl(linkUrl);
  }
}
