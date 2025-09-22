package com.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ui_component_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "component_id", "uid" }, name = "uk_ui_component_item_component_uid")
}, indexes = {
    @Index(columnList = "component_id,parent_id", name = "idx_ui_component_item_component_parent"),
    @Index(columnList = "level", name = "idx_ui_component_item_level"),
    @Index(columnList = "sort_order", name = "idx_ui_component_item_sort")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "component_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Component component;

  @NotBlank
  @Size(max = 100)
  @Pattern(regexp = "^[a-z0-9._-]+$")
  @Column(name = "uid", nullable = false, length = 100)
  private String uid;

  @NotBlank
  @Size(min = 36, max = 36)
  @Column(name = "uuid", nullable = false, length = 36)
  private String uuid;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private ComponentItem parent;

  @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
  private List<ComponentItem> children;

  @NotNull
  @Min(1)
  @Max(3)
  @Column(name = "level", nullable = false)
  private Integer level = 1;

  @Column(name = "visible", nullable = false)
  private boolean visible = true;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "created_by")
  private Long createdBy;

  @Column(name = "updated_by")
  private Long updatedBy;

  @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ComponentItemTranslation> translations;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (uuid == null || uuid.isBlank()) {
      uuid = java.util.UUID.randomUUID().toString();
    }
    if (level == null)
      level = 1;
    if (sortOrder == null)
      sortOrder = 0;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}

