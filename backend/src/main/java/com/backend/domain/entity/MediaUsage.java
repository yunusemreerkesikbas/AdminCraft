package com.backend.domain.entity;

import com.backend.domain.enums.MediaPurpose;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "media_usages", indexes = {
    @Index(columnList = "tenant_id, owner_type, owner_id", name = "idx_media_usage_owner"),
    @Index(columnList = "media_id", name = "idx_media_usage_media"),
    @Index(columnList = "is_cover", name = "idx_media_usage_cover")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_media_cover_per_owner", columnNames = { "tenant_id", "owner_type", "owner_id",
        "is_cover" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaUsage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false)
  private Long tenantId;

  @NotNull
  @Column(name = "media_id", nullable = false)
  private Long mediaId;

  @NotNull
  @Column(name = "owner_type", nullable = false, length = 50)
  private String ownerType; // e.g. PAGE, BLOCK, FORM_FIELD

  @NotNull
  @Column(name = "owner_id", nullable = false)
  private Long ownerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "purpose", nullable = false, length = 30)
  private MediaPurpose purpose = MediaPurpose.THUMBNAIL;

  @Column(name = "is_cover", nullable = false)
  private Boolean isCover = false;

  @Min(0)
  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder = 0;
}
