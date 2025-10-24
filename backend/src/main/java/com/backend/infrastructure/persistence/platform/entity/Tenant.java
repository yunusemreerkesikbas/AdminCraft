package com.backend.infrastructure.persistence.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

  @Column(name = "default_language", length = 10)
  private String defaultLanguage;

  @Column(name = "supported_languages", columnDefinition = "JSON")
  private String supportedLanguages;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

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
