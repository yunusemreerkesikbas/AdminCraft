package com.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Media i18n entity for localized metadata.
 * Stores alt text, title, and description per language.
 */
@Entity
@Table(name = "media_i18n", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "media_id", "language" }, name = "uk_media_i18n_lang")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MediaI18n extends BaseI18nEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "media_id", nullable = false)
  private Media media;

  @Size(max = 500, message = "validation.media.alt.text.size")
  @Column(name = "alt_text")
  private String altText;

  @Size(max = 255, message = "validation.media.title.size")
  @Column(name = "title")
  private String title;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;
}
