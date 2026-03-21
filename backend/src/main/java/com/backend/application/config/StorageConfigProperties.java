package com.backend.application.config;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Application-layer storage configuration properties.
 * Moved from infrastructure layer for Clean Architecture compliance.
 */
@ConfigurationProperties(prefix = "craftive.storage")
@Data
public class StorageConfigProperties {

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
  private S3 s3 = new S3();

  @Getter
  @Setter
  @ToString(exclude = {"accessKey", "secretKey"})
  public static class S3 {
    private String endpoint;
    private String bucket;
    private String region = "fra1";
    private String accessKey;
    private String secretKey;
    private String cdnBaseUrl;
    private boolean deleteLocalAfterMigration = false;
  }

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
