package com.backend.domain.entity;

import com.backend.domain.enums.EntryFieldType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "entry_field_definitions", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "component_type_id", "field_key" }, name = "uk_type_field")
}, indexes = {
        @Index(columnList = "component_type_id", name = "idx_field_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntryFieldDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "component_type_id", nullable = false)
    private Long componentTypeId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "field_key", nullable = false, length = 50)
    private String fieldKey;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false, length = 20)
    private EntryFieldType fieldType;

    @Size(max = 50)
    @Column(name = "migration_version", length = 50)
    private String migrationVersion;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(name = "is_required")
    private Boolean isRequired = false;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "min_value", precision = 10, scale = 2)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 10, scale = 2)
    private BigDecimal maxValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}



