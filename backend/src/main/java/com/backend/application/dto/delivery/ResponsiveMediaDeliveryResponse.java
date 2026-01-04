package com.backend.application.dto.delivery;

import com.backend.domain.entity.Media;
import com.backend.domain.entity.MediaI18n;
import com.backend.domain.entity.ResponsiveMediaSet;
import com.backend.domain.enums.Language;

import lombok.Builder;
import lombok.Getter;

/**
 * CMS Delivery DTO for responsive media.
 * Contains desktop and mobile media with full details for frontend rendering.
 */
@Getter
@Builder
public class ResponsiveMediaDeliveryResponse {

  private MediaDelivery desktop;
  private MediaDelivery mobile;

  /**
   * Individual media delivery object with all rendering details.
   */
  @Getter
  @Builder
  @lombok.ToString
  public static class MediaDelivery {
    private String uid;
    private String mime;
    private String url;
    private String altText;
    private String title;
    private Integer width;
    private Integer height;

    /**
     * Create from Media entity with i18n lookup.
     */
    public static MediaDelivery from(Media media, Language language) {
      if (media == null) {
        return null;
      }

      String altText = null;
      String title = null;

      // Find translation for the requested language
      if (media.getTranslations() != null) {
        for (MediaI18n i18n : media.getTranslations()) {
          if (i18n.getLanguage() == language) {
            altText = i18n.getAltText();
            title = i18n.getTitle();
            break;
          }
        }
      }

      return MediaDelivery.builder()
          .uid(media.getUid())
          .mime(media.getMimeType())
          .url(media.getPublicUrl())
          .altText(altText)
          .title(title)
          .width(media.getWidth())
          .height(media.getHeight())
          .build();
    }
  }

  /**
   * Factory method to create from ResponsiveMediaSet entity.
   */
  public static ResponsiveMediaDeliveryResponse from(ResponsiveMediaSet entity, Language language) {
    if (entity == null) {
      return null;
    }

    return ResponsiveMediaDeliveryResponse.builder()
        .desktop(MediaDelivery.from(entity.getDesktopMedia(), language))
        .mobile(MediaDelivery.from(entity.getMobileMedia(), language))
        .build();
  }

  /**
   * Check if this response has any media.
   */
  public boolean hasAnyMedia() {
    return desktop != null || mobile != null;
  }
}
