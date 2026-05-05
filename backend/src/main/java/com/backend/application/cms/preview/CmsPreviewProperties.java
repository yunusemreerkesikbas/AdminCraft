package com.backend.application.cms.preview;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration for the CMS SmartEdit preview pipeline.
 *
 * <pre>
 * app:
 *   cms:
 *     preview:
 *       secret: ${CMS_PREVIEW_SECRET:&#123;dev placeholder&#125;}
 *       ttl-seconds: 900            # 15 minutes
 *       storefront-base-url: http://localhost:3000
 * </pre>
 *
 * <p>Picked up automatically via {@code @ConfigurationPropertiesScan} on
 * {@code CraftiveApplication}. Startup validation lives in
 * {@code CmsPreviewTicketService} (it has the {@code Environment} dependency
 * needed to differentiate dev from prod).</p>
 */
@ConfigurationProperties(prefix = "app.cms.preview")
@Data
public class CmsPreviewProperties {

  public static final String DEV_PLACEHOLDER_SECRET =
      "DEV_ONLY_CMS_PREVIEW_SECRET_DO_NOT_USE_IN_PRODUCTION_AAAAAAAAAA";

  /** Minimum secret length in bytes. Matches the HMAC-SHA256 block size (RFC 2104). */
  public static final int MIN_SECRET_BYTES = 32;

  /** HMAC secret used to sign preview tickets. Must be at least 32 bytes in non-dev profiles. */
  private String secret = DEV_PLACEHOLDER_SECRET;

  /** Ticket lifetime in seconds. Defaults to 15 minutes. */
  private long ttlSeconds = 900L;

  /** Storefront origin used to build preview URLs returned to the admin UI. */
  private String storefrontBaseUrl = "http://localhost:3000";
}
