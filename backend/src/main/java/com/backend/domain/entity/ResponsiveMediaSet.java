package com.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * ResponsiveMediaSet: Contains Desktop and Mobile media references.
 * Used for responsive image delivery across different device types.
 * 
 * Usage:
 * - Component Level: Background/hero images with desktop/mobile variants
 * - Entry Level: Per-entry images like carousel slides, feature cards
 */
@Entity
@Table(name = "responsive_media_set", indexes = {
    @Index(columnList = "desktop_media_id", name = "idx_responsive_desktop"),
    @Index(columnList = "mobile_media_id", name = "idx_responsive_mobile")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ResponsiveMediaSet extends BaseEntity {

  @NotBlank
  @Size(max = 100)
  @Column(nullable = false, length = 100, unique = true)
  private String code;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "desktop_media_id")
  private Media desktopMedia;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mobile_media_id")
  private Media mobileMedia;

  /**
   * Check if this set has at least one media assigned.
   */
  public boolean hasAnyMedia() {
    return desktopMedia != null || mobileMedia != null;
  }

  /**
   * Check if both desktop and mobile media are assigned.
   */
  public boolean isComplete() {
    return desktopMedia != null && mobileMedia != null;
  }
}
