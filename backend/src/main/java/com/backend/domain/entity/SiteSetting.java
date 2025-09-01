package com.backend.domain.entity;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.SettingType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "site_settings", uniqueConstraints = {
    @UniqueConstraint(name = "uk_site_setting_key_language", columnNames = { "setting_key", "language" })
}, indexes = {
    @Index(name = "idx_site_setting_language", columnList = "language"),
    @Index(name = "idx_site_setting_category", columnList = "category"),
    @Index(name = "idx_site_setting_public", columnList = "is_public"),
    @Index(name = "idx_site_setting_type", columnList = "setting_type")
})
@Data
public class SiteSetting {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 100)
  @Column(name = "setting_key", nullable = false, length = 100)
  private String settingKey;

  @Lob
  @Column(name = "setting_value", columnDefinition = "TEXT")
  private String settingValue; // JSON or plain value as string

  @Enumerated(EnumType.STRING)
  @Column(name = "language")
  private Language language; // null means global

  @Enumerated(EnumType.STRING)
  @Column(name = "setting_type", nullable = false, length = 20)
  private SettingType settingType = SettingType.TEXT;

  @Size(max = 50)
  @Column(name = "category", length = 50)
  private String category = "general";

  @Size(max = 100)
  @Column(name = "display_name", length = 100)
  private String displayName;

  @Lob
  @Column(name = "description")
  private String description;

  @Column(name = "is_public", nullable = false)
  private Boolean isPublic = Boolean.FALSE;

  @Column(name = "sort_order")
  private Integer sortOrder = 0;

  @Column(name = "updated_by")
  private Long updatedBy;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
