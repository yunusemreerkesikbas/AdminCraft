package com.backend.domain.entity;

import com.backend.domain.enums.ComponentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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

    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Size(max = 100)
    @Column(name = "button_text", length = 100)
    private String buttonText;

    @Size(max = 500)
    @Column(name = "button_url", length = 500)
    private String buttonUrl;

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



