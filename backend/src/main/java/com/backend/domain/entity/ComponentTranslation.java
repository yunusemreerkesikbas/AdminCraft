package com.backend.domain.entity;

import com.backend.domain.enums.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ui_component_translations", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "component_id", "language" }, name = "uk_ui_component_translation_lang")
}, indexes = {
    @Index(columnList = "component_id", name = "idx_ui_comp_tr_component"),
    @Index(columnList = "language", name = "idx_ui_comp_tr_language")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentTranslation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Column(name = "component_id", nullable = false)
  private Long componentId;

  @Enumerated(EnumType.STRING)
  @Column(name = "language", nullable = false, length = 5)
  private Language language;

  @Size(max = 200)
  @Column(name = "title")
  private String title;

  @Size(max = 300)
  @Column(name = "subtitle")
  private String subtitle;

  @Lob
  @Column(name = "data", columnDefinition = "LONGTEXT")
  private String data;
}
