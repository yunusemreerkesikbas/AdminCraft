package com.backend.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ComponentMediaLink: Tracks media usage across components and entries.
 * Enables the "Linked Components" feature in Media Detail dialog.
 * 
 * Link Types:
 * - RESPONSIVE_DESKTOP: Component/Entry desktop image
 * - RESPONSIVE_MOBILE: Component/Entry mobile image
 * - ENTRY_MEDIA: Single media in entry field (legacy support)
 */
@Entity
@Table(name = "component_media_links", indexes = {
    @Index(columnList = "media_id", name = "idx_cml_media"),
    @Index(columnList = "component_id", name = "idx_cml_component"),
    @Index(columnList = "entry_id", name = "idx_cml_entry"),
    @Index(columnList = "responsive_set_id", name = "idx_cml_responsive_set")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentMediaLink {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "component_id", nullable = false)
  private Long componentId;

  @Column(name = "media_id", nullable = false)
  private Long mediaId;

  @Enumerated(EnumType.STRING)
  @Column(name = "link_type", nullable = false, length = 20)
  private LinkType linkType = LinkType.ENTRY_MEDIA;

  @Column(name = "entry_id")
  private Long entryId;

  @Column(name = "responsive_set_id")
  private Long responsiveSetId;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  public enum LinkType {
    RESPONSIVE_DESKTOP,
    RESPONSIVE_MOBILE,
    ENTRY_MEDIA
  }

  /**
   * Factory method for component-level responsive link.
   */
  public static ComponentMediaLink forComponentResponsive(
      Long componentId,
      Long mediaId,
      Long responsiveSetId,
      boolean isDesktop) {
    ComponentMediaLink link = new ComponentMediaLink();
    link.setComponentId(componentId);
    link.setMediaId(mediaId);
    link.setResponsiveSetId(responsiveSetId);
    link.setLinkType(isDesktop ? LinkType.RESPONSIVE_DESKTOP : LinkType.RESPONSIVE_MOBILE);
    return link;
  }

  /**
   * Factory method for entry-level responsive link.
   */
  public static ComponentMediaLink forEntryResponsive(
      Long componentId,
      Long entryId,
      Long mediaId,
      Long responsiveSetId,
      boolean isDesktop) {
    ComponentMediaLink link = new ComponentMediaLink();
    link.setComponentId(componentId);
    link.setEntryId(entryId);
    link.setMediaId(mediaId);
    link.setResponsiveSetId(responsiveSetId);
    link.setLinkType(isDesktop ? LinkType.RESPONSIVE_DESKTOP : LinkType.RESPONSIVE_MOBILE);
    return link;
  }
}
