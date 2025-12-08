package com.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "page_slots", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "uid" }, name = "uk_page_slot_uid"),
    @UniqueConstraint(columnNames = { "page_id", "slot_name" }, name = "uk_page_slot")
}, indexes = {
    @Index(columnList = "page_id", name = "idx_page_slots_page"),
    @Index(columnList = "is_shared", name = "idx_page_slots_shared"),
    @Index(columnList = "is_active", name = "idx_page_slots_active")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PageSlot extends BaseEntity {

  @Column(name = "page_id")
  private Long pageId;

  @NotBlank
  @Size(max = 50)
  @Column(name = "slot_name", nullable = false, length = 50)
  private String slotName;

  @NotBlank
  @Size(max = 20)
  @Column(name = "position", nullable = false, length = 20)
  private String position;

  @Column(name = "sort_order")
  private Integer sortOrder = 0;

  @Column(name = "is_active")
  private Boolean isActive = true;

  @Column(name = "is_shared")
  private Boolean isShared = false;
}
