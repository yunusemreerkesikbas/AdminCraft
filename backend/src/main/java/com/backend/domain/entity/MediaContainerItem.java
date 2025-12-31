package com.backend.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Media container item entity.
 * Junction table linking container to format and generated media.
 */
@Entity
@Table(name = "media_container_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "container_id", "format_id" }, name = "uk_container_format")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaContainerItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "container_id", nullable = false)
  private MediaContainer container;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "format_id", nullable = false)
  private MediaFormat format;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "media_id", nullable = false)
  private Media media;
}
