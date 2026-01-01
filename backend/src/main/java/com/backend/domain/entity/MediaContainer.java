package com.backend.domain.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Media container entity (SAP Hybris pattern).
 * Groups master media with its generated format variants.
 */
@Entity
@Table(name = "media_containers")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MediaContainer extends BaseEntity {

  @NotBlank(message = "validation.media.container.code.required")
  @Size(max = 100, message = "validation.media.container.code.size")
  @Column(name = "code", nullable = false, unique = true)
  private String code;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "master_media_id", nullable = false)
  private Media masterMedia;

  @OneToMany(mappedBy = "container", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<MediaContainerItem> items = new ArrayList<>();

  // Business methods
  public int getVariantCount() {
    return items != null ? items.size() : 0;
  }

  /**
   * Get media for a specific format code.
   */
  public Media getMediaForFormat(String formatCode) {
    if (items == null)
      return null;
    return items.stream()
        .filter(item -> item.getFormat() != null && formatCode.equals(item.getFormat().getCode()))
        .map(MediaContainerItem::getMedia)
        .findFirst()
        .orElse(null);
  }

  /**
   * Check if container has a specific format.
   */
  public boolean hasFormat(String formatCode) {
    return getMediaForFormat(formatCode) != null;
  }
}
