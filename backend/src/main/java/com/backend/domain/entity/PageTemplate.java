package com.backend.domain.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "page_templates", indexes = {
    @Index(columnList = "is_active", name = "idx_page_template_active")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PageTemplate extends BaseEntity {

  @NotBlank
  @Size(max = 100)
  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Size(max = 500)
  @Column(name = "description", length = 500)
  private String description;

  @Column(name = "is_system")
  private Boolean isSystem = false;

  @Column(name = "is_active")
  private Boolean isActive = true;

  @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @OrderBy("sortOrder ASC")
  private List<TemplateSlot> slots = new ArrayList<>();
}
