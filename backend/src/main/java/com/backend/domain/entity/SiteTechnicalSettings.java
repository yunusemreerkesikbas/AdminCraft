package com.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing technical settings for a site.
 * One-to-one relationship with Site entity.
 * Contains robots.txt, sitemap settings, verification codes, and custom scripts.
 */
@Entity
@Table(name = "site_technical_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteTechnicalSettings {

    @Id
    @Column(name = "site_id")
    private Long siteId;

    /**
     * Read-only relationship for lazy loading Site data.
     * The actual FK is managed via siteId field directly.
     * This avoids "detached entity passed to persist" issues with @MapsId.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", insertable = false, updatable = false)
    private Site site;

    // Search Engine Settings
    @Column(name = "robots_txt", columnDefinition = "TEXT")
    private String robotsTxt;

    @Column(name = "sitemap_enabled", nullable = false)
    @Builder.Default
    private Boolean sitemapEnabled = true;

    @Column(name = "indexing_enabled", nullable = false)
    @Builder.Default
    private Boolean indexingEnabled = true;

    // Verification Codes
    @Size(max = 100)
    @Column(name = "google_verification", length = 100)
    private String googleVerification;

    @Size(max = 100)
    @Column(name = "bing_verification", length = 100)
    private String bingVerification;

    @Size(max = 100)
    @Column(name = "yandex_verification", length = 100)
    private String yandexVerification;

    // Custom Scripts
    @Column(name = "head_scripts", columnDefinition = "TEXT")
    private String headScripts;

    @Column(name = "body_start_scripts", columnDefinition = "TEXT")
    private String bodyStartScripts;

    @Column(name = "body_end_scripts", columnDefinition = "TEXT")
    private String bodyEndScripts;

    // Cookie Consent Settings
    @Column(name = "cookie_consent_enabled", nullable = false)
    @Builder.Default
    private Boolean cookieConsentEnabled = false;

    @Column(name = "cookie_consent_text", columnDefinition = "TEXT")
    private String cookieConsentText;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Get default robots.txt content.
     */
    public static String getDefaultRobotsTxt() {
        return """
            User-agent: *
            Allow: /

            Sitemap: /sitemap.xml
            """;
    }

    /**
     * Check if the site should be indexed by search engines.
     */
    public boolean shouldBeIndexed() {
        return Boolean.TRUE.equals(indexingEnabled);
    }

    /**
     * Get the effective robots.txt content.
     * Returns noindex directive if indexing is disabled.
     */
    public String getEffectiveRobotsTxt() {
        if (!shouldBeIndexed()) {
            return """
                User-agent: *
                Disallow: /
                """;
        }
        return robotsTxt != null ? robotsTxt : getDefaultRobotsTxt();
    }
}
