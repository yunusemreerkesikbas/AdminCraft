package com.backend.infrastructure.storage;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "admincraft.storage")
@Data
public class StorageProperties {

  private String provider = "local";

  private String basePath = "uploads";

  private String tempPath = "temp";

  private long maxFileSize = 10 * 1024 * 1024;

  private Set<String> allowedMimeTypes = Set.of(
      "image/jpeg",
      "image/png",
      "image/gif",
      "image/webp",
      "application/pdf",
      "video/mp4",
      "audio/mpeg");

  private Set<String> blockedExtensions = Set.of(
      "exe", "bat", "cmd", "scr", "js", "vbs", "ps1", "sh");

  private Processing processing = new Processing();

  @Data
  public static class Processing {

    private boolean asyncEnabled = true;

    private int thumbnailQuality = 80;
    private Set<String> autoGenerateFormats = Set.of("THUMBNAIL", "SMALL", "MEDIUM");

    private Set<String> supportedImageTypes = Set.of(
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp");
  }
}
