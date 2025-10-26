package com.backend.domain.entity;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.SettingType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

@Entity
@Table(name = "site_settings", uniqueConstraints = {
    @UniqueConstraint(name = "uk_site_setting_key_lang", columnNames = { "setting_key", "language" })
}, indexes = {
    @Index(name = "idx_site_setting_type", columnList = "setting_type")
})
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

  public boolean isPublicSetting() {
    return Boolean.TRUE.equals(this.isPublic);
  }

  public boolean isValidForLanguage(Language targetLanguage) {
    // Global settings (language is null) apply to all languages
    if (this.language == null) {
      return true;
    }
    return Objects.equals(this.language, targetLanguage);
  }

  public boolean hasValidKey() {
    if (settingKey == null || settingKey.trim().isEmpty()) {
      return false;
    }
    Pattern pattern = Pattern.compile("^[a-z0-9._-]+$");
    return pattern.matcher(settingKey).matches();
  }

  public boolean requiresTranslation() {
    return settingType == SettingType.I18N_TEXT;
  }

  public void updateValue(String newValue, Long userId) {
    this.settingValue = newValue;
    this.updatedBy = userId;
    this.updatedAt = LocalDateTime.now();
  }

  public boolean hasValidUrl() {
    if (settingType != SettingType.URL || settingValue == null) {
      return true; // Not a URL setting or empty
    }
    return settingValue.matches("^https?://.*$");
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getSettingKey() {
    return settingKey;
  }

  public void setSettingKey(String settingKey) {
    this.settingKey = settingKey;
  }

  public String getSettingValue() {
    return settingValue;
  }

  public void setSettingValue(String settingValue) {
    this.settingValue = settingValue;
  }

  public Language getLanguage() {
    return language;
  }

  public void setLanguage(Language language) {
    this.language = language;
  }

  public SettingType getSettingType() {
    return settingType;
  }

  public void setSettingType(SettingType settingType) {
    this.settingType = settingType;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Boolean getIsPublic() {
    return isPublic;
  }

  public void setIsPublic(Boolean isPublic) {
    this.isPublic = isPublic;
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  public Long getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(Long updatedBy) {
    this.updatedBy = updatedBy;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    SiteSetting that = (SiteSetting) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(settingKey, that.settingKey) &&
        Objects.equals(language, that.language);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, settingKey, language);
  }

  @Override
  public String toString() {
    return "SiteSetting{" +
        "id=" + id +
        ", settingKey='" + settingKey + '\'' +
        ", language=" + language +
        ", settingType=" + settingType +
        ", category='" + category + '\'' +
        '}';
  }
}
