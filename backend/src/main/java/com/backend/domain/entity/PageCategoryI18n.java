package com.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "page_category_i18n", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "tenant_id", "category_id", "language" }, name = "uk_page_category_i18n_cat_lang"),
    @UniqueConstraint(columnNames = { "tenant_id", "uid" }, name = "uk_page_category_i18n_uid_tenant"),
    @UniqueConstraint(columnNames = { "tenant_id", "language", "url" }, name = "uk_page_category_i18n_url")
}, indexes = {
    @Index(columnList = "category_id", name = "idx_cat_i18n_category"),
    @Index(columnList = "tenant_id, language", name = "idx_cat_i18n_tenant_lang"),
    @Index(columnList = "tenant_id, language, url", name = "idx_cat_i18n_url")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PageCategoryI18n extends BaseI18nEntity {

  @NotNull
  @Column(name = "category_id", nullable = false)
  private Long categoryId;

  @Size(max = 150)
  @Column(name = "url", length = 150)
  private String url;

  @Size(max = 200)
  @Column(name = "title", length = 200)
  private String title;

  @Size(max = 60)
  @Column(name = "meta_title", length = 60)
  private String metaTitle;

  @Size(max = 160)
  @Column(name = "meta_description", length = 160)
  private String metaDescription;

  @Column(name = "active", nullable = false)
  private Boolean active = true;
}
