package com.backend.application.cms.preview;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "app.cms.preview")
@Data
public class CmsPreviewProperties {

  public static final String DEV_PLACEHOLDER_SECRET =
      "DEV_ONLY_CMS_PREVIEW_SECRET_DO_NOT_USE_IN_PRODUCTION_AAAAAAAAAA";

  public static final int MIN_SECRET_BYTES = 32;

  private String secret = DEV_PLACEHOLDER_SECRET;
  private long ttlSeconds = 900L;
  private String storefrontBaseUrl = "http://localhost:3000";
}
