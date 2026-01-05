package com.backend.domain.entity;

import com.backend.domain.enums.ComponentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "component_entries", indexes = {
        @Index(columnList = "component_id, sort_order", name = "idx_component_order"),
        @Index(columnList = "status", name = "idx_entry_status")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ComponentEntry extends BaseEntity {

    @NotNull
    @Column(name = "component_id", nullable = false)
    private Long componentId;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_visible")
    private Boolean isVisible = true;

    @Column(name = "style_classes", length = 500)
    private String styleClasses;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private ComponentStatus status = ComponentStatus.DRAFT;

    /**
     * Responsive media set for entry-level Desktop/Mobile images.
     * Used for entry backgrounds, hero images, etc.
     * EAGER fetch to prevent LazyInitializationException in DTO mappings.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsive_id")
    private ResponsiveMediaSet responsiveMedia;
}
