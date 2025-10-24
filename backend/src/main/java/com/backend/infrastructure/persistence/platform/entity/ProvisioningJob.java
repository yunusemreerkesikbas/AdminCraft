package com.backend.infrastructure.persistence.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "provisioning_jobs", schema = "platform_management")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvisioningJob {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false)
  private Long tenantId;

  @Column(nullable = false, length = 50)
  private String type;

  @Column(columnDefinition = "JSON")
  private String payload;

  @Column(nullable = false, length = 20)
  private String status;

  @Column
  private Integer progress;

  @Column(columnDefinition = "TEXT")
  private String error;

  @Column(name = "correlation_id", length = 36)
  private String correlationId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "started_at")
  private LocalDateTime startedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
