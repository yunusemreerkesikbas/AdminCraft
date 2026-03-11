package com.backend.domain.entity;

import java.time.LocalDateTime;

import com.backend.domain.enums.Language;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "newsletter_subscriber_subscriptions", uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_newsletter_subscriber_template_type",
        columnNames = { "subscriber_id", "template_key" }
    )
})
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "subscriber")
@NoArgsConstructor
public class NewsletterSubscriberSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private NewsletterSubscriber subscriber;

    @Column(name = "template_key", nullable = false, length = 100)
    private String templateKey;

    @Column(name = "source", length = 120)
    private String source;

    @Column(name = "preferred_language", nullable = false, length = 10)
    private String preferredLanguage = Language.EN.name();

    @Column(name = "permission", nullable = false)
    private Boolean permission = Boolean.TRUE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
