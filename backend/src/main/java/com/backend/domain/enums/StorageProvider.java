package com.backend.domain.enums;

/**
 * Storage provider types for media files.
 * Determines where the actual file binary is stored.
 */
public enum StorageProvider {
  LOCAL, // Local file system
  S3, // Amazon S3
  AZURE, // Azure Blob Storage
  CLOUDINARY // Cloudinary CDN
}
