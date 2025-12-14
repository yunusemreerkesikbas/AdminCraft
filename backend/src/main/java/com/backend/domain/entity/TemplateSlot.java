package com.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "template_slots", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "template_id", "slot_name" }, name = "uk_template_slot")
}, indexes = {
    @Index(columnList = "template_id", name = "idx_template_slot_template")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSlot extends BaseEntity {

  @Column(name = "template_id", nullable = false)
  private Long templateId;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "template_id", insertable = false, updatable = false)
  private PageTemplate template;

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

  @Column(name = "is_required")
  private Boolean isRequired = false;

  @Column(name = "max_components")
  private Integer maxComponents;

  @Column(name = "allowed_types", columnDefinition = "JSON")
  private String allowedTypes;
}
