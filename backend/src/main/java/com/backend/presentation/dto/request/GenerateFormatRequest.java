package com.backend.presentation.dto.request;

import com.backend.domain.enums.CropMode;
import com.backend.domain.enums.OutputFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for generating a single format variant.
 * Supports both preset formats (by code) and custom dimensions.
 */
public record GenerateFormatRequest(
    @Size(max = 50, message = "validation.media.format.code.size") String formatCode, // Preset: "THUMBNAIL", "SMALL",
                                                                                      // etc.

    @Min(value = 1, message = "validation.media.format.width.min") @Max(value = 10000, message = "validation.media.format.width.max") Integer customWidth,

    @Min(value = 1, message = "validation.media.format.height.min") @Max(value = 10000, message = "validation.media.format.height.max") Integer customHeight,

    @Min(value = 1, message = "validation.media.format.quality.min") @Max(value = 100, message = "validation.media.format.quality.max") Integer quality,

    CropMode cropMode,

    OutputFormat outputFormat) {
  /**
   * Check if this is a preset format request.
   */
  public boolean isPreset() {
    return formatCode != null && !formatCode.isBlank();
  }

  /**
   * Check if this is a custom format request.
   */
  public boolean isCustom() {
    return customWidth != null && customHeight != null;
  }

  /**
   * Get effective quality (default 85 if not specified).
   */
  public int getEffectiveQuality() {
    return quality != null ? quality : 85;
  }

  /**
   * Get effective crop mode (default FIT if not specified).
   */
  public CropMode getEffectiveCropMode() {
    return cropMode != null ? cropMode : CropMode.FIT;
  }

  /**
   * Get effective output format (default ORIGINAL if not specified).
   */
  public OutputFormat getEffectiveOutputFormat() {
    return outputFormat != null ? outputFormat : OutputFormat.ORIGINAL;
  }
}
