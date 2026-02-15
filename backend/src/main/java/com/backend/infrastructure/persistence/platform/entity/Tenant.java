package com.backend.infrastructure.persistence.platform.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenants", schema = "platform_management")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String subdomain;

  @Column(name = "company_name", nullable = false)
  private String companyName;

  @Column(name = "custom_domain", length = 255)
  private String customDomain;

  @Column(name = "db_host", length = 100)
  private String dbHost;

  @Column(name = "db_port")
  private Integer dbPort;

  @Column(name = "database_name", nullable = false, unique = true, length = 100)
  private String databaseName;

  @Column(nullable = false, length = 20)
  private String status;

  @Column(name = "two_factor_policy", length = 20)
  @Builder.Default
  private String twoFactorPolicy = "DISABLED";

  @Column(name = "default_language", length = 10)
  private String defaultLanguage;

  @Column(name = "supported_languages", columnDefinition = "JSON")
  private String supportedLanguages;

  @Column(name = "admin_email", length = 255)
  private String adminEmail;

  @Column(name = "admin_name", length = 100)
  private String adminName;

  @Column(name = "currency", nullable = false, length = 3)
  @Builder.Default
  private String currency = "TRY";

  @Column(name = "has_admin_user", nullable = false)
  @Builder.Default
  private Boolean hasAdminUser = false;

  @Column(name = "storage_used_mb")
  private Long storageUsedMb;

  @Column(name = "last_backup_at")
  private LocalDateTime lastBackupAt;

  @Column(name = "activated_at")
  private LocalDateTime activatedAt;

  @Column(length = 1000)
  private String notes;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "tenant")
  @Builder.Default
  private List<TenantModule> modules = new ArrayList<>();

  @OneToMany(mappedBy = "tenant")
  @Builder.Default
  private List<ProvisioningJob> provisioningJobs = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
