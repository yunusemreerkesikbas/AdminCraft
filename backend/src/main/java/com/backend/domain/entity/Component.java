package com.backend.domain.entity;

import com.backend.domain.enums.ComponentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "components", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "uid" }, name = "uk_component_uid")
}, indexes = {
        @Index(columnList = "component_type_id", name = "idx_component_type"),
        @Index(columnList = "status", name = "idx_component_status")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Component extends BaseEntity {

    @NotNull
    @Column(name = "component_type_id", nullable = false)
    private Long componentTypeId;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_visible")
    private Boolean isVisible = true;

    @Size(max = 500)
    @Column(name = "style_classes", length = 500)
    private String styleClasses;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private ComponentStatus status = ComponentStatus.DRAFT;
}
