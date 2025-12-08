package com.backend.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "slot_components", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "slot_id", "component_id" }, name = "uk_slot_component")
}, indexes = {
    @Index(columnList = "slot_id", name = "idx_slot_components_slot"),
    @Index(columnList = "component_id", name = "idx_slot_components_component"),
    @Index(columnList = "slot_id, sort_order", name = "idx_slot_components_order")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotComponent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Column(name = "slot_id", nullable = false)
  private Long slotId;

  @NotNull
  @Column(name = "component_id", nullable = false)
  private Long componentId;

  @Column(name = "sort_order")
  private Integer sortOrder = 0;

  @Column(name = "is_visible")
  private Boolean isVisible = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }
}
