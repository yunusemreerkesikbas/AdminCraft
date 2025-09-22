package com.backend.domain.entity;

import com.backend.domain.enums.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ui_component_item_translations", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "item_id", "language" }, name = "uk_ui_component_item_translation_item_language")
}, indexes = {
    @Index(columnList = "item_id,language", name = "idx_ui_component_item_tr_item_language"),
    @Index(columnList = "language", name = "idx_ui_component_item_tr_language")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentItemTranslation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "item_id", nullable = false)
  private ComponentItem item;

  @Enumerated(EnumType.STRING)
  @Column(name = "language", nullable = false, length = 5)
  private Language language;

  @Size(max = 200)
  @Column(name = "title", length = 200)
  private String title;

  @Column(name = "subtitle")
  private String subtitle;

  @Size(max = 255)
  @Column(name = "url", length = 255)
  private String url;

  @Size(max = 60)
  @Column(name = "seo_title", length = 60)
  private String seoTitle;

  @Size(max = 160)
  @Column(name = "seo_description", length = 160)
  private String seoDescription;

  @Size(max = 255)
  @Column(name = "seo_keywords", length = 255)
  private String seoKeywords;
}

