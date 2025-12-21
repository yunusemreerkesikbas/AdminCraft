package com.backend.domain.entity;

import com.backend.domain.enums.Language;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "page_template_i18n", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "template_id", "language" }, name = "uk_template_i18n_lang"),
    @UniqueConstraint(columnNames = { "uid" }, name = "uk_template_i18n_uid")
}, indexes = {
    @Index(columnList = "uuid", name = "idx_template_i18n_uuid"),
    @Index(columnList = "template_id", name = "idx_template_i18n_template_id"),
    @Index(columnList = "language", name = "idx_template_i18n_language")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "template" })
@NoArgsConstructor
@AllArgsConstructor
public class PageTemplateI18n extends BaseI18nEntity {

  @NotNull
  @Column(name = "template_id", nullable = false)
  private Long templateId;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "template_id", insertable = false, updatable = false)
  private PageTemplate template;

  @NotBlank
  @Size(max = 100)
  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Size(max = 500)
  @Column(name = "description", length = 500)
  private String description;

  public PageTemplateI18n(Long templateId, Language language, String name, String description) {
    this.templateId = templateId;
    setLanguage(language);
    this.name = name;
    this.description = description;
  }
}
