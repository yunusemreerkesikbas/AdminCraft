package com.backend.domain.entity;

import com.backend.domain.enums.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sites")
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"tenant"})
@ToString(exclude = {"tenant"})
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Site adı boş olamaz")
    @Size(max = 100, message = "Site adı 100 karakterden uzun olamaz")
    @Column(name = "site_name", nullable = false, length = 100)
    private String siteName;

    @Size(max = 500, message = "Açıklama 500 karakterden uzun olamaz")
    @Column(name = "description", length = 500)
    private String description;

    @ElementCollection(targetClass = Language.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "site_languages", joinColumns = @JoinColumn(name = "site_id"))
    @Column(name = "language")
    private Set<Language> enabledLanguages = new HashSet<>();

    @NotNull(message = "Varsayılan dil belirtilmelidir")
    @Enumerated(EnumType.STRING)
    @Column(name = "default_language", nullable = false)
    private Language defaultLanguage = Language.TR;

    @NotNull(message = "Tenant ID boş olamaz")
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    // Site configuration fields
    @Column(name = "domain")
    private String domain;

    @Column(name = "custom_domain")
    private String customDomain;

    @Column(name = "is_ssl_enabled", nullable = false)
    private Boolean sslEnabled = true;

    @Column(name = "is_published", nullable = false)
    private Boolean published = false;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "theme_name")
    private String themeName = "default";

    @Column(name = "theme")
    private String theme = "default";

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "favicon_url")
    private String faviconUrl;

    @Column(name = "primary_color", length = 7)
    private String primaryColor = "#1976d2";

    @Column(name = "secondary_color", length = 7)
    private String secondaryColor = "#dc004e";

    // Theme customization fields
    @Column(name = "color_scheme")
    private String colorScheme = "default";

    @Column(name = "font_family")
    private String fontFamily = "Inter";

    // SEO Configuration
    @Size(max = 60, message = "Site başlığı 60 karakterden uzun olamaz")
    @Column(name = "site_title", length = 60)
    private String siteTitle;

    @Size(max = 160, message = "Site açıklaması 160 karakterden uzun olamaz")
    @Column(name = "site_description", length = 160)
    private String siteDescription;

    @Size(max = 200, message = "Anahtar kelimeler 200 karakterden uzun olamaz")
    @Column(name = "site_keywords", length = 200)
    private String siteKeywords;

    // Additional SEO fields for mapper compatibility
    @Size(max = 60, message = "Meta başlık 60 karakterden uzun olamaz")
    @Column(name = "meta_title", length = 60)
    private String metaTitle;

    @Size(max = 160, message = "Meta açıklama 160 karakterden uzun olamaz")
    @Column(name = "meta_description", length = 160)
    private String metaDescription;

    @Size(max = 200, message = "Meta anahtar kelimeler 200 karakterden uzun olamaz")
    @Column(name = "meta_keywords", length = 200)
    private String metaKeywords;

    // Social Media Configuration
    @Column(name = "og_image_url")
    private String ogImageUrl;

    @Column(name = "twitter_handle", length = 50)
    private String twitterHandle;

    @Column(name = "facebook_page_url")
    private String facebookPageUrl;

    // Analytics Configuration
    @Column(name = "google_analytics_id", length = 50)
    private String googleAnalyticsId;

    @Column(name = "google_tag_manager_id", length = 50)
    private String googleTagManagerId;

    // Custom code injection
    @Column(name = "custom_code", columnDefinition = "TEXT")
    private String customCode;

    // Site Status
    @Column(name = "maintenance_mode", nullable = false)
    private Boolean maintenanceMode = false;

    @Column(name = "maintenance_message", length = 500)
    private String maintenanceMessage;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", insertable = false, updatable = false)
    private Tenant tenant;

    // Business Methods
    public boolean isLanguageEnabled(Language language) {
        return enabledLanguages.contains(language);
    }

    public void addEnabledLanguage(Language language) {
        if (language != null) {
            this.enabledLanguages.add(language);
        }
    }

    public void removeEnabledLanguage(Language language) {
        if (language != null && !language.equals(defaultLanguage)) {
            this.enabledLanguages.remove(language);
        }
    }

    public boolean canBePublished() {
        return siteName != null && !siteName.trim().isEmpty() &&
               !enabledLanguages.isEmpty() &&
               defaultLanguage != null &&
               enabledLanguages.contains(defaultLanguage);
    }

    public void publish() {
        if (!canBePublished()) {
            throw new IllegalStateException("Site yayınlanmak için gerekli koşulları sağlamıyor");
        }
        this.published = true;
        this.publishedAt = LocalDateTime.now();
    }

    public void unpublish() {
        this.published = false;
        this.publishedAt = null;
    }

    public void enableMaintenanceMode(String message) {
        this.maintenanceMode = true;
        this.maintenanceMessage = message;
    }

    public void disableMaintenanceMode() {
        this.maintenanceMode = false;
        this.maintenanceMessage = null;
    }

    public boolean isAccessible() {
        return published && !maintenanceMode && active;
    }

    public String getFullDomain() {
        if (customDomain != null && !customDomain.trim().isEmpty()) {
            return customDomain;
        }
        return domain;
    }

    public String getSiteUrl() {
        String protocol = sslEnabled ? "https://" : "http://";
        return protocol + getFullDomain();
    }

    // Convenience methods for mapper compatibility
    public Boolean getIsPublished() {
        return published;
    }

    public void setIsPublished(Boolean published) {
        this.published = published;
        if (published && this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        } else if (!published) {
            this.publishedAt = null;
        }
    }

    public Boolean getIsActive() {
        return active;
    }

    public void setIsActive(Boolean active) {
        this.active = active;
    }

    // Constructor
    public Site() {
        this.enabledLanguages.add(Language.TR); // Default language support
    }
}