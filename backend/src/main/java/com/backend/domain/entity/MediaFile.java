package com.backend.domain.entity;

import com.backend.domain.enums.Language;
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

    // Optional JSON describing generated variants (desktop/mobile)
    @Column(name = "variants", columnDefinition = "TEXT")
    private String variants; // JSON: {"desktop":{...},"mobile":{...}}

    // Multi-language alt text
    @Size(max = 255, message = "validation.alt.text.tr.size")
    @Column(name = "alt_text_tr")
    private String altTextTr;

    @Size(max = 255, message = "validation.alt.text.en.size")
    @Column(name = "alt_text_en")
    private String altTextEn;

    // Multi-language description
    @Size(max = 500, message = "validation.description.tr.size")
    @Column(name = "description_tr")
    private String descriptionTr;

    @Size(max = 500, message = "validation.description.en.size")
    @Column(name = "description_en")
    private String descriptionEn;

    // Multi-language title/caption
    @Size(max = 200, message = "validation.title.tr.size")
    @Column(name = "title_tr")
    private String titleTr;

    @Size(max = 200, message = "validation.title.en.size")
    @Column(name = "title_en")
    private String titleEn;

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

    // Business methods
    public String getAltText(Language language) {
        return switch (language) {
            case TR -> altTextTr;
            case EN -> altTextEn;
            default -> altTextTr; // fallback
        };
    }

    public String getDescription(Language language) {
        return switch (language) {
            case TR -> descriptionTr;
            case EN -> descriptionEn;
            default -> descriptionTr; // fallback
        };
    }

    public String getTitle(Language language) {
        return switch (language) {
            case TR -> titleTr;
            case EN -> titleEn;
            default -> titleTr; // fallback
        };
    }

    public void setAltText(Language language, String altText) {
        switch (language) {
            case TR -> this.altTextTr = altText;
            case EN -> this.altTextEn = altText;
        }
    }

    public void setDescription(Language language, String description) {
        switch (language) {
            case TR -> this.descriptionTr = description;
            case EN -> this.descriptionEn = description;
        }
    }

    public void setTitle(Language language, String title) {
        switch (language) {
            case TR -> this.titleTr = title;
            case EN -> this.titleEn = title;
        }
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

    public String getDimensions() {
        if (width != null && height != null) {
            return width + "x" + height;
        }
        return null;
    }
}