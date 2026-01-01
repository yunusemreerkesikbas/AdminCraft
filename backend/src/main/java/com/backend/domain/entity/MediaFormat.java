package com.backend.domain.entity;

import com.backend.domain.enums.CropMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Media format definition entity.
 * Defines dimensions and quality for generated image variants.
 */
@Entity
@Table(name = "media_formats")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MediaFormat extends BaseEntity {

  @NotBlank(message = "validation.media.format.code.required")
  @Size(max = 50, message = "validation.media.format.code.size")
  @Column(name = "code", nullable = false, unique = true)
  private String code;

  @NotBlank(message = "validation.media.format.name.required")
  @Size(max = 100, message = "validation.media.format.name.size")
  @Column(name = "name", nullable = false)
  private String name;

  @Min(value = 1, message = "validation.media.format.width.min")
  @Max(value = 10000, message = "validation.media.format.width.max")
  @Column(name = "width", nullable = false)
  private Integer width;

  @Min(value = 1, message = "validation.media.format.height.min")
  @Max(value = 10000, message = "validation.media.format.height.max")
  @Column(name = "height", nullable = false)
  private Integer height;

  @Min(value = 1, message = "validation.media.format.quality.min")
  @Max(value = 100, message = "validation.media.format.quality.max")
  @Column(name = "quality")
  private Integer quality = 80;

  @Enumerated(EnumType.STRING)
  @Column(name = "crop_mode", length = 20)
  private CropMode cropMode = CropMode.FIT;

  @Column(name = "is_system")
  private Boolean isSystem = false;

  // Business methods
  public boolean isSystemFormat() {
    return Boolean.TRUE.equals(isSystem);
  }

  public String getDimensions() {
    return width + "x" + height;
  }
}
