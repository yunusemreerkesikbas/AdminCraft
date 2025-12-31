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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Media folder entity for hierarchical organization.
 * Supports nested folder structure with path-based navigation.
 */
@Entity
@Table(name = "media_folders")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MediaFolder extends BaseEntity {

  @NotBlank(message = "validation.media.folder.code.required")
  @Size(max = 100, message = "validation.media.folder.code.size")
  @Column(name = "code", nullable = false)
  private String code;

  @NotBlank(message = "validation.media.folder.name.required")
  @Size(max = 255, message = "validation.media.folder.name.size")
  @Column(name = "name", nullable = false)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private MediaFolder parent;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MediaFolder> children = new ArrayList<>();

  @NotBlank(message = "validation.media.folder.path.required")
  @Size(max = 768, message = "validation.media.folder.path.size")
  @Column(name = "path", nullable = false, unique = true, length = 768)
  private String path;

  @Min(value = 0, message = "validation.media.folder.depth.min")
  @Max(value = 10, message = "validation.media.folder.depth.max")
  @Column(name = "depth")
  private Integer depth = 0;

  // Business methods
  public boolean isRoot() {
    return parent == null;
  }

  public boolean hasChildren() {
    return children != null && !children.isEmpty();
  }

  public int getChildCount() {
    return children != null ? children.size() : 0;
  }

  /**
   * Build full path from parent hierarchy.
   */
  public void updatePath() {
    if (parent == null) {
      this.path = "/" + code + "/";
    } else {
      this.path = parent.getPath() + code + "/";
    }
    this.depth = parent == null ? 0 : parent.getDepth() + 1;
  }
}
