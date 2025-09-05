package com.backend.domain.enums;

/**
 * Lifecycle status of a media file
 */
public enum MediaStatus {
  STAGED, // uploaded but not finalized/attached
  ACTIVE, // ready for use
  ARCHIVED, // hidden from normal listings
  DELETED // soft-deleted (if needed)
}
