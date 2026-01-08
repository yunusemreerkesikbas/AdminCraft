package com.backend.domain.entity;

import java.time.LocalDateTime;

import com.backend.domain.enums.PageStatus;
import com.backend.domain.exception.PageCannotBePublishedException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "page_i18n", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "page_id", "language" }, name = "uk_page_i18n_page_lang"),
        @UniqueConstraint(columnNames = { "uid" }, name = "uk_page_i18n_uid"),
        @UniqueConstraint(columnNames = { "language", "canonical_url" }, name = "uk_page_i18n_canonical_url")
}, indexes = {
        @Index(columnList = "page_id", name = "idx_page_i18n_page"),
        @Index(columnList = "language", name = "idx_page_i18n_lang"),
        @Index(columnList = "language, canonical_url", name = "idx_page_i18n_canonical_url"),
        @Index(columnList = "language, status", name = "idx_page_i18n_status")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PageI18n extends BaseI18nEntity {

    @NotNull
    @Column(name = "page_id", nullable = false)
    private Long pageId;

    @Size(max = 200)
    @Column(length = 200)
    private String name;

    @Size(max = 255)
    @Column(name = "canonical_url", length = 255)
    private String canonicalUrl;

    @Size(max = 200)
    @Column(length = 200)
    private String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private PageStatus status = PageStatus.DRAFT;

    public boolean canBePublished() {
        return status == PageStatus.DRAFT &&
                title != null && !title.trim().isEmpty() &&
                canonicalUrl != null && !canonicalUrl.trim().isEmpty();
    }

    public void publish() {
        if (!canBePublished()) {
            throw new PageCannotBePublishedException(
                    "PageI18n (id=" + getId() + ", pageId=" + pageId + ", language=" + getLanguage() +
                            ") cannot be published. Missing required fields: title and/or canonicalUrl.");
        }
        this.status = PageStatus.PUBLISHED;
    }

    public void unpublish() {
        if (this.status == PageStatus.PUBLISHED) {
            this.status = PageStatus.DRAFT;
        }
    }

    public void schedule(LocalDateTime scheduledAt) {
        // Logic moved to Page entity or handled implicitly by status
        this.status = PageStatus.SCHEDULED;
    }

    public void archive() {
        this.status = PageStatus.ARCHIVED;
        // Dates are now on Page entity
    }

    public void restore() {
        if (this.status == PageStatus.ARCHIVED) {
            this.status = PageStatus.DRAFT;
        }
    }
}
