package com.backend.domain.enums;

/**
 * Status of a media file in the system.
 */
public enum MediaStatus {
  PROCESSING, // File is being processed (e.g., generating thumbnails)
  ACTIVE, // File is ready for use
  ARCHIVED, // File is archived but not deleted
  FAILED // Processing failed
}
