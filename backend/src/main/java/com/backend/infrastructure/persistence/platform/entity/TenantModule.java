package com.backend.infrastructure.persistence.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_modules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantModule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false)
  private Long tenantId;

  @Column(name = "module_code", nullable = false, length = 50)
  private String moduleCode;

  @Column(nullable = false, length = 20)
  private String status;

  @Column(name = "target_version", length = 20)
  private String targetVersion;

  @Column(name = "installed_at")
  private LocalDateTime installedAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", insertable = false, updatable = false)
  private Tenant tenant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "module_code", referencedColumnName = "code", insertable = false, updatable = false)
  private ModuleCatalog moduleCatalog;

  @PrePersist
  protected void onCreate() {
    if (installedAt == null) {
      installedAt = LocalDateTime.now();
    }
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
