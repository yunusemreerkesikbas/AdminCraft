package com.backend.domain.enums;

/**
 * Crop mode for image resizing operations.
 */
public enum CropMode {
  FIT, // Fit within dimensions, preserve aspect ratio
  FILL, // Fill dimensions, may crop
  COVER // Cover dimensions exactly, center crop
}
