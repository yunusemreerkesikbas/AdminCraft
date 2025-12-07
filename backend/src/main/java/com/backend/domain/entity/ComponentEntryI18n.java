package com.backend.domain.entity;

import java.time.LocalDateTime;

import com.backend.domain.enums.ComponentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "component_entry_i18n", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "entry_id", "language" }, name = "uk_entry_language"),
        @UniqueConstraint(columnNames = { "uid" }, name = "uk_entry_i18n_uid")
}, indexes = {
        @Index(columnList = "entry_id", name = "idx_entry_i18n_entry"),
        @Index(columnList = "language", name = "idx_entry_i18n_language"),
        @Index(columnList = "status", name = "idx_entry_i18n_status")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ComponentEntryI18n extends BaseI18nEntity {

    @NotNull
    @Column(name = "entry_id", nullable = false)
    private Long entryId;

    @Size(max = 255)
    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private ComponentStatus status = ComponentStatus.DRAFT;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "custom_data", columnDefinition = "JSON")
    private String customData;

    public void publish() {
        this.status = ComponentStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void unpublish() {
        if (this.status == ComponentStatus.PUBLISHED) {
            this.status = ComponentStatus.DRAFT;
            this.publishedAt = null;
        }
    }
}
