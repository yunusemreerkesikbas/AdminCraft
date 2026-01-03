package com.backend.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.backend.domain.enums.MediaStatus;
import com.backend.domain.enums.StorageProvider;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Main media entity representing an uploaded file.
 * Replaces the legacy MediaFile entity with proper BaseEntity pattern.
 */
@Entity
@Table(name = "media")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Media extends BaseEntity {

  // File Properties
  @NotBlank(message = "validation.media.original.name.required")
  @Size(max = 255, message = "validation.media.original.name.size")
  @Column(name = "original_name", nullable = false)
  private String originalName;

  @NotBlank(message = "validation.media.file.name.required")
  @Size(max = 255, message = "validation.media.file.name.size")
  @Column(name = "file_name", nullable = false, unique = true)
  private String fileName;

  @NotBlank(message = "validation.media.file.path.required")
  @Size(max = 500, message = "validation.media.file.path.size")
  @Column(name = "file_path", nullable = false)
  private String filePath;

  @NotBlank(message = "validation.media.mime.type.required")
  @Size(max = 100, message = "validation.media.mime.type.size")
  @Column(name = "mime_type", nullable = false)
  private String mimeType;

  @Size(max = 20, message = "validation.media.file.extension.size")
  @Column(name = "file_extension")
  private String fileExtension;

  @Min(value = 0, message = "validation.media.file.size.min")
  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  // Image/Video Properties
  @Column(name = "width")
  private Integer width;

  @Column(name = "height")
  private Integer height;

  @Column(name = "duration")
  private Integer duration; // Video duration in seconds

  // Organization
  @Column(name = "tags", columnDefinition = "JSON")
  private String tags; // JSON array of tags

  // Access Control
  @Column(name = "is_public")
  private Boolean isPublic = true;

  @Column(name = "uploaded_by")
  private Long uploadedBy;

  // Storage Provider
  @Enumerated(EnumType.STRING)
  @Column(name = "storage_provider", length = 20)
  private StorageProvider storageProvider = StorageProvider.LOCAL;

  @Size(max = 1000, message = "validation.media.external.url.size")
  @Column(name = "external_url")
  private String externalUrl;

  @Size(max = 255, message = "validation.media.external.id.size")
  @Column(name = "external_id")
  private String externalId;

  // Metadata
  @Column(name = "metadata", columnDefinition = "JSON")
  private String metadata; // EXIF data, etc.

  @Column(name = "usage_count")
  private Integer usageCount = 0;

  // Status
  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20)
  private MediaStatus status = MediaStatus.ACTIVE;

  @Column(name = "last_accessed_at")
  private LocalDateTime lastAccessedAt;

  // i18n translations
  @OneToMany(mappedBy = "media", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<MediaI18n> translations = new ArrayList<>();

  // Business methods
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

  public String getDimensions() {
    if (width != null && height != null) {
      return width + "x" + height;
    }
    return null;
  }

  public String getPublicUrl() {
    if (externalUrl != null) {
      return externalUrl;
    }
    return "/api/media/files/" + fileName;
  }

  public void incrementUsage() {
    this.usageCount++;
    this.lastAccessedAt = LocalDateTime.now();
  }

  public void recordAccess() {
    this.lastAccessedAt = LocalDateTime.now();
  }
}
