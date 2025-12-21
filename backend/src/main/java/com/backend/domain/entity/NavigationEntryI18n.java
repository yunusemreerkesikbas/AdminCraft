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
@Table(name = "navigation_entry_i18n", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "entry_id", "language" }, name = "uk_nav_entry_i18n_lang"),
    @UniqueConstraint(columnNames = { "uid" }, name = "uk_nav_entry_i18n_uid")
}, indexes = {
    @Index(columnList = "uuid", name = "idx_nav_entry_i18n_uuid"),
    @Index(columnList = "entry_id", name = "idx_nav_entry_i18n_entry_id"),
    @Index(columnList = "language", name = "idx_nav_entry_i18n_language")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "entry" })
@NoArgsConstructor
@AllArgsConstructor
public class NavigationEntryI18n extends BaseI18nEntity {

  @NotNull
  @Column(name = "entry_id", nullable = false)
  private Long entryId;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "entry_id", insertable = false, updatable = false)
  private NavigationEntry entry;

  @NotBlank
  @Size(max = 200)
  @Column(name = "link_name", nullable = false, length = 200)
  private String linkName;

  public NavigationEntryI18n(Long entryId, Language language, String linkName) {
    this.entryId = entryId;
    setLanguage(language);
    this.linkName = linkName;
  }
}
