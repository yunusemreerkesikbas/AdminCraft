package com.backend.domain.entity;

import com.backend.domain.enums.ComponentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "component_i18n", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "component_id", "language" }, name = "uk_component_i18n_component_lang"),
        @UniqueConstraint(columnNames = { "uid" }, name = "uk_component_i18n_uid")
}, indexes = {
        @Index(columnList = "component_id", name = "idx_component_i18n_component"),
        @Index(columnList = "language", name = "idx_component_i18n_language"),
        @Index(columnList = "language, status", name = "idx_component_i18n_status")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ComponentI18n extends BaseI18nEntity {

    @NotNull
    @Column(name = "component_id", nullable = false)
    private Long componentId;

    @Size(max = 200)
    @Column(length = 200)
    private String title;

    @Size(max = 200)
    @Column(length = 200)
    private String subtitle;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private ComponentStatus status = ComponentStatus.DRAFT;
}
