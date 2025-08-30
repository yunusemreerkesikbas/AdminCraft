package com.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "page_categories", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "tenant_id", "slug" }, name = "uk_page_category_slug_tenant")
}, indexes = {
    @Index(columnList = "tenant_id", name = "idx_page_category_tenant"),
    @Index(columnList = "parent_id", name = "idx_page_category_parent")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Column(name = "tenant_id", nullable = false)
  private Long tenantId;

  @NotBlank
  @Size(max = 100)
  @Column(nullable = false)
  private String name;

  @NotBlank
  @Size(max = 150)
  @Column(nullable = false)
  private String slug;

  @Column(name = "parent_id")
  private Long parentId;
}
