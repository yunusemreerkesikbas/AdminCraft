package com.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "page_sections", indexes = {
    @Index(columnList = "page_id", name = "idx_page_section_page"),
    @Index(columnList = "display_order", name = "idx_page_section_order")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageSection {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Column(name = "page_id", nullable = false)
  private Long pageId;

  @Column(name = "type", length = 50)
  private String type;

  @Column(name = "display_order")
  private Integer displayOrder = 0;

  @Lob
  @Column(name = "data", columnDefinition = "TEXT")
  private String data; // JSON
}
