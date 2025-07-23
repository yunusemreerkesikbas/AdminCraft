package com.backend.domain.entity;

import com.backend.domain.enums.Language;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "validation.content.type.name.required")
    @Size(max = 50, message = "validation.content.type.name.size")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "validation.content.type.display.name.required")
    @Size(max = 100, message = "validation.content.type.display.name.size")
    @Column(name = "display_name", nullable = false)
    private String displayName;
    
    @Size(max = 100, message = "validation.content.type.display.name.tr.size")
    @Column(name = "display_name_tr")
    private String displayNameTr;
    
    @Size(max = 100, message = "validation.content.type.display.name.en.size")
    @Column(name = "display_name_en")
    private String displayNameEn;
    
    @Size(max = 500, message = "validation.description.size")
    private String description;
    
    @Size(max = 500, message = "validation.description.tr.size")
    @Column(name = "description_tr")
    private String descriptionTr;
    
    @Size(max = 500, message = "validation.description.en.size")
    @Column(name = "description_en")
    private String descriptionEn;
    
    @Column(columnDefinition = "TEXT")
    private String fields; // JSON schema for custom fields
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "supports_multi_language")
    private Boolean supportsMultiLanguage = true;
    
    @Column(name = "supports_seo")
    private Boolean supportsSeo = true;
    
    @Column(name = "supports_scheduling")
    private Boolean supportsScheduling = true;
    
    @Column(name = "supports_comments")
    private Boolean supportsComments = false;
    
    @Column(name = "requires_approval")
    private Boolean requiresApproval = false;
    
    @Column(name = "is_system_type")
    private Boolean isSystemType = false;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Size(max = 50, message = "validation.icon.size")
    private String icon = "document";
    
    @Size(max = 20, message = "validation.color.size")
    private String color = "#3B82F6";
    
    @Column(name = "max_items")
    private Integer maxItems; // null = unlimited
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
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
    
    // Business methods
    public boolean isValidForLanguage(Language language) {
        return !supportsMultiLanguage || language != null;
    }
    
    public boolean canCreateMoreItems(long currentCount) {
        return maxItems == null || currentCount < maxItems;
    }
    
    public String getLocalizedDisplayName(Language language) {
        return switch (language) {
            case TR -> displayNameTr != null ? displayNameTr : displayName;
            case EN -> displayNameEn != null ? displayNameEn : displayName;
            default -> displayName;
        };
    }
    
    public String getLocalizedDescription(Language language) {
        return switch (language) {
            case TR -> descriptionTr != null ? descriptionTr : description;
            case EN -> descriptionEn != null ? descriptionEn : description;
            default -> description;
        };
    }
    
    public boolean isBuiltInType() {
        return isSystemType && ("page".equals(name) || "post".equals(name) || "menu".equals(name));
    }
    
    public boolean canBeDeleted() {
        return !isSystemType;
    }
}