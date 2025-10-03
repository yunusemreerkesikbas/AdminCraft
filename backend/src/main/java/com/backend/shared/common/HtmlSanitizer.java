package com.backend.shared.common;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public final class HtmlSanitizer {
  private HtmlSanitizer() {
  }

  public static String sanitizeRichText(String html) {
    if (html == null || html.isBlank()) {
      return html;
    }
    return Jsoup.clean(html, Safelist.relaxed()
        .addAttributes(":all", "class", "style", "id")
        .addAttributes("a", "target", "rel"));
  }
}
