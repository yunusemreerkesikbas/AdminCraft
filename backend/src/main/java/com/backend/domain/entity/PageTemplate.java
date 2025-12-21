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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "page_templates", indexes = {
    @Index(columnList = "is_active", name = "idx_page_template_active")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "slots", "i18nContent" })
@NoArgsConstructor
@AllArgsConstructor
public class PageTemplate extends BaseEntity {

  @Column(name = "is_system")
  private Boolean isSystem = false;

  @Column(name = "is_active")
  private Boolean isActive = true;

  @ToString.Exclude
  @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @OrderBy("sortOrder ASC")
  private List<TemplateSlot> slots = new ArrayList<>();

  @ToString.Exclude
  @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<PageTemplateI18n> i18nContent = new ArrayList<>();
}
