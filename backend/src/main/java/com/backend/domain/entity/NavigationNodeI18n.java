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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "navigation_node_i18n", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "node_id", "language" }, name = "uk_nav_node_i18n_lang"),
    @UniqueConstraint(columnNames = { "uid" }, name = "uk_nav_node_i18n_uid")
}, indexes = {
    @Index(columnList = "uuid", name = "idx_nav_node_i18n_uuid"),
    @Index(columnList = "node_id", name = "idx_nav_node_i18n_node_id"),
    @Index(columnList = "language", name = "idx_nav_node_i18n_language")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "node" })
@NoArgsConstructor
@AllArgsConstructor
public class NavigationNodeI18n extends BaseI18nEntity {

  @NotNull
  @Column(name = "node_id", nullable = false)
  private Long nodeId;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "node_id", insertable = false, updatable = false)
  private NavigationNode node;

  @Size(max = 200)
  @Column(name = "title", length = 200)
  private String title;

  // Convenience constructor
  public NavigationNodeI18n(Long nodeId, Language language, String title) {
    this.nodeId = nodeId;
    setLanguage(language);
    this.title = title;
  }
}
