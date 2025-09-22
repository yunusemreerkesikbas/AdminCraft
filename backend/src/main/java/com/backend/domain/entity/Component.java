package com.backend.domain.entity;

import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.ComponentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ui_components", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "tenant_id", "type", "component_key" }, name = "uk_ui_component_tenant_type_key")
}, indexes = {
    @Index(columnList = "tenant_id", name = "idx_ui_component_tenant"),
    @Index(columnList = "tenant_id,type", name = "idx_ui_component_tenant_type"),
    @Index(columnList = "tenant_id,type,status", name = "idx_ui_component_tenant_type_status"),
    @Index(columnList = "tenant_id,type,sort_order,status", name = "idx_ui_component_tenant_type_sort_status"),
    @Index(columnList = "type", name = "idx_ui_component_type"),
    @Index(columnList = "status", name = "idx_ui_component_status"),
    @Index(columnList = "sort_order", name = "idx_ui_component_sort")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Component {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Column(name = "tenant_id", nullable = false)
  private Long tenantId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 30)
  private ComponentType type;

  @NotBlank
  @Size(max = 100)
  @Column(name = "component_key", nullable = false, length = 100)
  private String key;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private ComponentStatus status = ComponentStatus.ACTIVE;

  @Column(name = "visible", nullable = false)
  private boolean visible = true;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder = 0;

  @Size(max = 255)
  @Column(name = "style_classes", length = 255)
  private String styleClasses;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  @Column(name = "updated_by")
  private Long updatedBy;

  @OneToMany(mappedBy = "component", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ComponentTranslation> translations;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (status == null) {
      status = ComponentStatus.ACTIVE;
    }
    if (sortOrder == null) {
      sortOrder = 0;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public void activate() {
    this.status = ComponentStatus.ACTIVE;
  }

  public void deactivate() {
    this.status = ComponentStatus.INACTIVE;
  }

  public boolean isActive() {
    return ComponentStatus.ACTIVE.equals(this.status);
  }

  public boolean isVisible() {
    return this.visible;
  }

  public void setVisibility(boolean visible) {
    this.visible = visible;
  }

  public void updateSortOrder(Integer sortOrder) {
    if (sortOrder != null && sortOrder >= 0) {
      this.sortOrder = sortOrder;
    }
  }

  public boolean isValidForTenant(Long tenantId) {
    return this.tenantId != null && this.tenantId.equals(tenantId);
  }
}