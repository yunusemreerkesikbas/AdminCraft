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
@EqualsAndHashCode(callSuper = false)
@ToString
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
    @Column(name = "domain")
    private String domain;

    @Column(name = "custom_domain")
    private String customDomain;

    @Column(name = "is_ssl_enabled", nullable = false)
    private Boolean sslEnabled = true;

    @Column(name = "is_published", nullable = false)
    private Boolean published = false;

    @Column(name = "theme_name")
    private String themeName = "default";
    @Size(max = 60, message = "Site başlığı 60 karakterden uzun olamaz")
    @Column(name = "site_title", length = 60)
    private String siteTitle;

    @Size(max = 160, message = "Site açıklaması 160 karakterden uzun olamaz")
    @Column(name = "site_description", length = 160)
    private String siteDescription;

    @Size(max = 200, message = "Anahtar kelimeler 200 karakterden uzun olamaz")
    @Column(name = "site_keywords", length = 200)
    private String siteKeywords;

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
        return published && !maintenanceMode;
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

    // Constructor
    public Site() {
        this.enabledLanguages.add(Language.TR); // Default language support
    }
}