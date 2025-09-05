package com.backend.domain.entity;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.MediaStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_files", uniqueConstraints = {
        @UniqueConstraint(columnNames = "file_name", name = "uk_media_filename")
}, indexes = {
        @Index(columnList = "tenant_id", name = "idx_media_tenant"),
        @Index(columnList = "file_name", name = "idx_media_filename"),
        @Index(columnList = "mime_type", name = "idx_media_mimetype"),
        @Index(columnList = "folder", name = "idx_media_folder"),
        @Index(columnList = "category", name = "idx_media_category"),
        @Index(columnList = "uploaded_by", name = "idx_media_uploader"),
        @Index(columnList = "created_at", name = "idx_media_created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "validation.original.name.required")
    @Size(max = 255, message = "validation.original.name.size")
    @Column(name = "original_name", nullable = false)
    private String originalName;

    @NotBlank(message = "validation.file.name.required")
    @Size(max = 255, message = "validation.file.name.size")
    @Column(name = "file_name", nullable = false, unique = true)
    private String fileName;

    @NotBlank(message = "validation.file.path.required")
    @Size(max = 500, message = "validation.file.path.size")
    @Column(name = "file_path", nullable = false)
    private String filePath;

    @NotBlank(message = "validation.mime.type.required")
    @Size(max = 100, message = "validation.mime.type.size")
    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Min(value = 0, message = "validation.file.size.min")
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Size(max = 50, message = "validation.file.extension.size")
    @Column(name = "file_extension")
    private String fileExtension;

    // Image properties
    private Integer width;
    private Integer height;

    @Column(name = "has_thumbnails")
    private Boolean hasThumbnails = false;

    @Size(max = 500, message = "validation.thumbnail.path.size")
    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    // Sprint 7: JSON variants structure for desktop/mobile variants
    // JSON: { desktop:{url,w,h}, mobile:{url,w,h} }
    @Column(name = "variants", columnDefinition = "json")
    private String variants;

    // Sprint 7: JSON-based i18n structure for localized metadata
    // JSON: { "tr": { title, subtitle, altText, seo{title, description, keywords} }, "en": { ... } }
    @Column(name = "i18n", columnDefinition = "json")
    private String i18n;
    
    // Content hash for de-duplication (Sprint 7 requirement)
    @Column(name = "content_hash", length = 64)
    private String contentHash; // SHA-256 hash
    
    // Sprint 7: Staged upload system
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MediaStatus status = MediaStatus.STAGED;

    // Organization
    @Size(max = 100, message = "validation.folder.size")
    private String folder = "uploads";

    @Size(max = 100, message = "validation.category.size")
    private String category = "general";

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags; // JSON array of tags

    // Access and usage
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "is_optimized")
    private Boolean isOptimized = false;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    // External storage
    @Size(max = 20, message = "validation.storage.provider.size")
    @Column(name = "storage_provider")
    private String storageProvider = "local"; // local, s3, cloudinary, etc.

    @Size(max = 500, message = "validation.external.url.size")
    @Column(name = "external_url")
    private String externalUrl;

    @Size(max = 100, message = "validation.external.id.size")
    @Column(name = "external_id")
    private String externalId;

    // Metadata
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON metadata (EXIF, duration, etc.)

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastAccessedAt = LocalDateTime.now();

        if (fileExtension == null && originalName != null) {
            int lastDotIndex = originalName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                fileExtension = originalName.substring(lastDotIndex + 1).toLowerCase();
            }
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Sprint 7: JSON-based i18n business methods
    public MediaI18nMetadata getI18nMetadata(Language language) {
        if (i18n == null || i18n.isEmpty()) {
            return new MediaI18nMetadata();
        }
        // Parse JSON and return metadata for specific language with fallback
        return parseI18nJson(language);
    }
    
    public String getAltText(Language language) {
        MediaI18nMetadata metadata = getI18nMetadata(language);
        return metadata != null ? metadata.getAltText() : null;
    }

    public String getDescription(Language language) {
        MediaI18nMetadata metadata = getI18nMetadata(language);
        return metadata != null ? metadata.getDescription() : null;
    }

    public String getTitle(Language language) {
        MediaI18nMetadata metadata = getI18nMetadata(language);
        return metadata != null ? metadata.getTitle() : null;
    }
    
    public String getSubtitle(Language language) {
        MediaI18nMetadata metadata = getI18nMetadata(language);
        return metadata != null ? metadata.getSubtitle() : null;
    }
    
    // Setter methods for i18n content
    public void setAltText(Language language, String altText) {
        // TODO: Implement JSON-based i18n update logic
        // This should parse the i18n JSON, update the altText for the given language, and save back
    }
    
    public void setDescription(Language language, String description) {
        // TODO: Implement JSON-based i18n update logic
        // This should parse the i18n JSON, update the description for the given language, and save back
    }
    
    public void setTitle(Language language, String title) {
        // TODO: Implement JSON-based i18n update logic
        // This should parse the i18n JSON, update the title for the given language, and save back
    }
    
    public void setSubtitle(Language language, String subtitle) {
        // TODO: Implement JSON-based i18n update logic
        // This should parse the i18n JSON, update the subtitle for the given language, and save back
    }
    
    private MediaI18nMetadata parseI18nJson(Language language) {
        // TODO: Implement JSON parsing logic with Jackson
        // This should parse the i18n JSON and return metadata for the requested language
        // If language not found, fallback to tenant default language
        return new MediaI18nMetadata();
    }
    
    // Inner class for i18n metadata structure
    public static class MediaI18nMetadata {
        private String title;
        private String subtitle; 
        private String altText;
        private String description;
        private MediaSeoMetadata seo;
        
        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getSubtitle() { return subtitle; }
        public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
        
        public String getAltText() { return altText; }
        public void setAltText(String altText) { this.altText = altText; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public MediaSeoMetadata getSeo() { return seo; }
        public void setSeo(MediaSeoMetadata seo) { this.seo = seo; }
    }
    
    // Inner class for SEO metadata
    public static class MediaSeoMetadata {
        private String title;
        private String description;
        private String[] keywords;
        
        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String[] getKeywords() { return keywords; }
        public void setKeywords(String[] keywords) { this.keywords = keywords; }
    }

    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public boolean isVideo() {
        return mimeType != null && mimeType.startsWith("video/");
    }

    public boolean isAudio() {
        return mimeType != null && mimeType.startsWith("audio/");
    }

    public boolean isDocument() {
        return mimeType != null && (mimeType.startsWith("application/") ||
                mimeType.startsWith("text/") ||
                mimeType.equals("application/pdf"));
    }

    public String getFileType() {
        if (isImage())
            return "image";
        if (isVideo())
            return "video";
        if (isAudio())
            return "audio";
        if (isDocument())
            return "document";
        return "other";
    }

    public String getFileSizeFormatted() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }

    public String getPublicUrl() {
        if (externalUrl != null) {
            return externalUrl;
        }
        return "/api/media/files/" + fileName;
    }

    public String getThumbnailUrl() {
        if (hasThumbnails && thumbnailPath != null) {
            return "/api/media/thumbnails/" + fileName;
        }
        return getPublicUrl();
    }

    public void incrementUsage() {
        this.usageCount++;
        this.lastAccessedAt = LocalDateTime.now();
    }

    public boolean canBeDeleted() {
        return usageCount == 0; // Only delete if not used anywhere
    }
    
    // Sprint 7: Staged upload system business methods
    public boolean isStaged() {
        return MediaStatus.STAGED.equals(status);
    }
    
    public boolean isActive() {
        return MediaStatus.ACTIVE.equals(status);
    }
    
    public void activate() {
        if (isStaged()) {
            this.status = MediaStatus.ACTIVE;
        }
    }
    
    public void archive() {
        if (isActive()) {
            this.status = MediaStatus.ARCHIVED;
        }
    }
    
    public boolean canBeActivated() {
        return isStaged();
    }
    
    // Sprint 7: Variant management methods
    public MediaVariant getDesktopVariant() {
        return parseVariantJson("desktop");
    }
    
    public MediaVariant getMobileVariant() {
        return parseVariantJson("mobile");
    }
    
    public void addVariant(String variantType, String url, int width, int height) {
        // TODO: Implement JSON variant addition logic
        // Update variants JSON with new variant
    }
    
    private MediaVariant parseVariantJson(String variantType) {
        // TODO: Implement JSON parsing for variants
        // Parse variants JSON and return specific variant
        return new MediaVariant();
    }
    
    // Inner class for variant structure
    public static class MediaVariant {
        private String url;
        private Integer width;
        private Integer height;
        
        public MediaVariant() {}
        
        public MediaVariant(String url, Integer width, Integer height) {
            this.url = url;
            this.width = width;
            this.height = height;
        }
        
        // Getters and setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public Integer getWidth() { return width; }
        public void setWidth(Integer width) { this.width = width; }
        
        public Integer getHeight() { return height; }
        public void setHeight(Integer height) { this.height = height; }
    }

    public String getDimensions() {
        if (width != null && height != null) {
            return width + "x" + height;
        }
        return null;
    }
}