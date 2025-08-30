package com.backend.domain.entity;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pages", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "tenant_id", "slug", "language" }, name = "uk_page_slug_tenant_lang")
}, indexes = {
    @Index(columnList = "tenant_id", name = "idx_page_tenant"),
    @Index(columnList = "slug", name = "idx_page_slug"),
    @Index(columnList = "status", name = "idx_page_status"),
    @Index(columnList = "language", name = "idx_page_language"),
    @Index(columnList = "published_at", name = "idx_page_published_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Page {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Column(name = "tenant_id", nullable = false)
  private Long tenantId;

  @NotBlank
  @Size(max = 200)
  @Column(nullable = false)
  private String title;

  @NotBlank
  @Size(max = 200)
  @Column(nullable = false)
  private String slug;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PageStatus status = PageStatus.DRAFT;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Language language = Language.TR;

  @Column(name = "category_id")
  private Long categoryId;

  // SEO
  @Size(max = 60)
  @Column(name = "meta_title")
  private String metaTitle;

  @Size(max = 160)
  @Column(name = "meta_description")
  private String metaDescription;

  @Size(max = 255)
  @Column(name = "canonical_url")
  private String canonicalUrl;

  // Content alanları
  // Açıklama: Kısa alt başlık bilgisi
  @Size(max = 200)
  @Column(name = "subtitle")
  private String subtitle;

  // Açıklama: CSS sınıfları (boşlukla ayrılmış)
  @Size(max = 255)
  @Column(name = "style_classes")
  private String styleClasses;

  // Açıklama: Zengin metin içerik (HTML/Delta)
  @Lob
  @Column(name = "description", columnDefinition = "LONGTEXT")
  private String description;

  // Açıklama: Render edilmiş ve sanitize edilmiş HTML
  @Lob
  @Column(name = "description_html", columnDefinition = "LONGTEXT")
  private String descriptionHtml;

  // Açıklama: Öne çıkan görsel URL'si ya da medya anahtarı
  @Size(max = 500)
  @Column(name = "featured_image")
  private String featuredImage;

  // Publication
  @Column(name = "published_at")
  private LocalDateTime publishedAt;

  @Column(name = "scheduled_at")
  private LocalDateTime scheduledAt;

  // Audit
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  @Column(name = "updated_by")
  private Long updatedBy;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
