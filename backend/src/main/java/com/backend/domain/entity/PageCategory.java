package com.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "page_categories", indexes = {
    @Index(columnList = "tenant_id", name = "idx_page_category_tenant"),
    @Index(columnList = "parent_id", name = "idx_page_category_parent"),
    @Index(columnList = "sort_order", name = "idx_page_category_sort")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PageCategory extends BaseEntity {

  @Column(name = "parent_id")
  private Long parentId;

  @Column(name = "active", nullable = false)
  private Boolean active = true;

  @Column(name = "style_classes", length = 255)
  private String styleClasses;

  @Column(name = "sort_order")
  private Integer sortOrder = 0;
}
