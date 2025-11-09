package com.backend.domain.entity;

import com.backend.domain.enums.ComponentStatus;
import com.backend.infrastructure.persistence.converter.JsonNodeConverter;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "component_i18n", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "component_id", "language" }, name = "uk_component_i18n_component_lang"),
        @UniqueConstraint(columnNames = { "uid" }, name = "uk_component_i18n_uid")
}, indexes = {
        @Index(columnList = "component_id", name = "idx_component_i18n_component"),
        @Index(columnList = "language", name = "idx_component_i18n_language"),
        @Index(columnList = "language, status", name = "idx_component_i18n_status"),
        @Index(columnList = "published_at", name = "idx_component_i18n_published")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ComponentI18n extends BaseI18nEntity {

    @NotNull
    @Column(name = "component_id", nullable = false)
    private Long componentId;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "base_localized_data", columnDefinition = "JSON")
    private JsonNode baseLocalizedData;

    @Column(name = "extended_localized_data", columnDefinition = "JSON")
    @Convert(converter = JsonNodeConverter.class)
    private JsonNode extendedLocalizedData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private ComponentStatus status = ComponentStatus.DRAFT;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public void publish() {
        this.status = ComponentStatus.ACTIVE;
        this.publishedAt = LocalDateTime.now();
    }

    public void unpublish() {
        if (this.status == ComponentStatus.ACTIVE) {
            this.status = ComponentStatus.DRAFT;
            this.publishedAt = null;
        }
    }
}
