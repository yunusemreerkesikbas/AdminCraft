package com.backend.domain.entity;

import com.backend.domain.enums.ProductFieldType;
import com.backend.domain.util.UuidUidGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_attribute_definitions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"uuid"}, name = "uk_product_attr_def_uuid"),
        @UniqueConstraint(columnNames = {"uid"}, name = "uk_product_attr_def_uid"),
        @UniqueConstraint(columnNames = {"product_type_id", "code"}, name = "uk_product_attr_type_code")
}, indexes = {
        @Index(columnList = "product_type_id", name = "idx_product_attr_def_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "uid", nullable = false, unique = true, length = 50)
    private String uid;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id", nullable = false)
    private ProductType productType;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String code;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false, length = 30)
    private ProductFieldType fieldType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (uuid == null) {
            uuid = UuidUidGenerator.generateUuid();
        }
        if (uid == null || uid.trim().isEmpty()) {
            uid = UuidUidGenerator.generateUid();
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
