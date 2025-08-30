package com.backend.domain.entity;

import com.backend.domain.enums.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "page_category_translations", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "tenant_id", "category_id", "language",
        "slug" }, name = "uk_page_category_i18n_slug")
}, indexes = {
    @Index(columnList = "tenant_id", name = "idx_cat_tr_tenant"),
    @Index(columnList = "category_id", name = "idx_cat_tr_category"),
    @Index(columnList = "language", name = "idx_cat_tr_lang")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageCategoryTranslation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Column(name = "tenant_id", nullable = false)
  private Long tenantId;

  @NotNull
  @Column(name = "category_id", nullable = false)
  private Long categoryId;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "language", nullable = false, length = 5)
  private Language language;

  @NotBlank
  @Size(max = 100)
  @Column(name = "name", nullable = false)
  private String name;

  @NotBlank
  @Size(max = 150)
  @Column(name = "slug", nullable = false)
  private String slug;

  @Lob
  @Column(name = "description", columnDefinition = "TEXT")
  private String description;
}
