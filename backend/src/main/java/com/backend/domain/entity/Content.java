package com.backend.domain.entity;

import com.backend.domain.enums.ContentStatus;
import com.backend.domain.enums.Language;
import com.backend.domain.exception.ContentCannotBePublishedException;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "contents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Content {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "validation.title.required")
    @Size(max = 200, message = "validation.title.size")
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "validation.slug.required")
    @Size(max = 200, message = "validation.slug.size")
    @Column(nullable = false)
    private String slug;
    
    @Size(max = 500, message = "validation.excerpt.size")
    private String excerpt;
    
    @Column(columnDefinition = "TEXT")
    private String data; // JSON content data
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status = ContentStatus.DRAFT;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language = Language.TR;
    
    @Column(name = "parent_content_id")
    private Long parentContentId; // For translations
    
    @Column(name = "content_type_id", nullable = false)
    private Long contentTypeId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    // SEO fields
    @Size(max = 60, message = "validation.meta.title.size")
    @Column(name = "meta_title")
    private String metaTitle;
    
    @Size(max = 160, message = "validation.meta.description.size")
    @Column(name = "meta_description")
    private String metaDescription;
    
    @Size(max = 100, message = "validation.meta.keywords.size")
    @Column(name = "meta_keywords")
    private String metaKeywords;
    
    @Size(max = 255, message = "validation.canonical.url.size")
    @Column(name = "canonical_url")
    private String canonicalUrl;
    
    @Size(max = 255, message = "validation.og.image.size")
    @Column(name = "og_image")
    private String ogImage;
    
    @Column(name = "no_index")
    private Boolean noIndex = false;
    
    @Column(name = "no_follow")
    private Boolean noFollow = false;
    
    // Publishing
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    // Ordering and organization
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @Column(name = "is_sticky")
    private Boolean isSticky = false;
    
    @Column(name = "view_count")
    private Long viewCount = 0L;
    
    @Column(name = "comment_count")
    private Integer commentCount = 0;
    
    @Column(name = "like_count")
    private Integer likeCount = 0;
    
    // Template and layout
    @Size(max = 50, message = "validation.template.size")
    private String template = "default";
    
    @Size(max = 50, message = "validation.layout.size")
    private String layout = "default";
    
    // Access control
    @Column(name = "is_password_protected")
    private Boolean isPasswordProtected = false;
    
    @Size(max = 255, message = "validation.content.password.size")
    @Column(name = "content_password")
    private String contentPassword;
    
    @Column(name = "requires_login")
    private Boolean requiresLogin = false;
    
    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @Column(name = "published_by")
    private Long publishedBy;
    
    @Size(max = 1000, message = "validation.notes.size")
    private String notes;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (slug == null || slug.isEmpty()) {
            slug = generateSlugFromTitle();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public boolean canBePublished() {
        return status == ContentStatus.DRAFT && 
               title != null && !title.trim().isEmpty() &&
               data != null && !data.trim().isEmpty();
    }
    
    public void publish() {
        if (!canBePublished()) {
            throw new ContentCannotBePublishedException(
                "Content with status " + status + " cannot be published");
        }
        this.status = ContentStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }
    
    public void unpublish() {
        if (status != ContentStatus.PUBLISHED) {
            throw new IllegalStateException(
                "Only published content can be unpublished");
        }
        this.status = ContentStatus.DRAFT;
        this.publishedAt = null;
    }
    
    public void archive() {
        this.status = ContentStatus.ARCHIVED;
    }
    
    public void schedule(LocalDateTime scheduledTime) {
        if (scheduledTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Scheduled time cannot be in the past");
        }
        this.status = ContentStatus.SCHEDULED;
        this.scheduledAt = scheduledTime;
    }
    
    public boolean isPublished() {
        return status == ContentStatus.PUBLISHED && 
               publishedAt != null && 
               publishedAt.isBefore(LocalDateTime.now()) &&
               (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }
    
    public boolean isTranslation() {
        return parentContentId != null;
    }
    
    public boolean canBeTranslated() {
        return !isTranslation();
    }
    
    public void incrementViewCount() {
        this.viewCount++;
    }
    
    public String getEffectiveMetaTitle() {
        return metaTitle != null && !metaTitle.trim().isEmpty() ? metaTitle : title;
    }
    
    public String getEffectiveMetaDescription() {
        if (metaDescription != null && !metaDescription.trim().isEmpty()) {
            return metaDescription;
        }
        if (excerpt != null && !excerpt.trim().isEmpty()) {
            return excerpt;
        }
        // Return truncated content if available
        if (data != null && data.length() > 160) {
            return data.substring(0, 157) + "...";
        }
        return data;
    }
    
    public boolean hasExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
    
    public boolean isScheduledForFuture() {
        return status == ContentStatus.SCHEDULED && 
               scheduledAt != null && 
               scheduledAt.isAfter(LocalDateTime.now());
    }
    
    private String generateSlugFromTitle() {
        if (title == null) {
            return "content-" + System.currentTimeMillis();
        }
        return title.toLowerCase()
                   .replaceAll("[^a-zA-Z0-9\\s-]", "")
                   .replaceAll("\\s+", "-")
                   .replaceAll("-+", "-")
                   .replaceAll("^-|-$", "");
    }
}