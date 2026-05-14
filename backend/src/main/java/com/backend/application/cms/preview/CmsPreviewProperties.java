package com.backend.application.cms.preview;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "app.cms.preview")
@Data
public class CmsPreviewProperties {

  private String secret;
  private long ttlSeconds = 900L;
}
