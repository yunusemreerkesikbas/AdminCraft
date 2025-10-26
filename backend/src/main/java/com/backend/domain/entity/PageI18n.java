package com.backend.domain.entity;

import com.backend.domain.enums.PageStatus;
import com.backend.domain.exception.PageCannotBePublishedException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "page_i18n", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "page_id", "language" }, name = "uk_page_i18n_page_lang"),
        @UniqueConstraint(columnNames = { "uid" }, name = "uk_page_i18n_uid"),
        @UniqueConstraint(columnNames = { "language", "url_path" }, name = "uk_page_i18n_url_path")
}, indexes = {
        @Index(columnList = "page_id", name = "idx_page_i18n_page"),
        @Index(columnList = "language", name = "idx_page_i18n_lang"),
        @Index(columnList = "language, url_path", name = "idx_page_i18n_url"),
        @Index(columnList = "language, status", name = "idx_page_i18n_status"),
        @Index(columnList = "published_at", name = "idx_page_i18n_published")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PageI18n extends BaseI18nEntity {

    @NotNull
    @Column(name = "page_id", nullable = false)
    private Long pageId;

    @Size(max = 255)
    @Column(name = "url_path", length = 255)
    private String urlPath;

    @Size(max = 200)
    @Column(length = 200)
    private String title;

    @Size(max = 200)
    @Column(length = 200)
    private String subtitle;

    @Size(max = 60)
    @Column(name = "meta_title", length = 60)
    private String metaTitle;

    @Size(max = 160)
    @Column(name = "meta_description", length = 160)
    private String metaDescription;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Lob
    @Column(name = "description_html", columnDefinition = "LONGTEXT")
    private String descriptionHtml;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private PageStatus status = PageStatus.DRAFT;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    public boolean canBePublished() {
        return status == PageStatus.DRAFT &&
                title != null && !title.trim().isEmpty() &&
                urlPath != null && !urlPath.trim().isEmpty();
    }

    public void publish() {
        if (!canBePublished()) {
            throw new PageCannotBePublishedException(
                    "PageI18n (id=" + getId() + ", pageId=" + pageId + ", language=" + getLanguage() +
                            ") cannot be published. Missing required fields: title and/or urlPath.");
        }
        this.status = PageStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.scheduledAt = null;
    }

    public void unpublish() {
        if (this.status == PageStatus.PUBLISHED) {
            this.status = PageStatus.DRAFT;
            this.publishedAt = null;
            this.scheduledAt = null;
        }
    }

    public void schedule(LocalDateTime scheduledAt) {
        if (scheduledAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Cannot schedule publication in the past. Scheduled time: " + scheduledAt);
        }

        if (!canBePublished()) {
            throw new PageCannotBePublishedException(
                    "PageI18n (id=" + getId() + ", pageId=" + pageId + ", language=" + getLanguage() +
                            ") cannot be scheduled. Missing required fields: title and/or urlPath.");
        }

        this.scheduledAt = scheduledAt;
        this.status = PageStatus.SCHEDULED;
    }

    public void archive() {
        this.status = PageStatus.ARCHIVED;
        this.publishedAt = null;
        this.scheduledAt = null;
    }

    public void restore() {
        if (this.status == PageStatus.ARCHIVED) {
            this.status = PageStatus.DRAFT;
        }
    }
}
