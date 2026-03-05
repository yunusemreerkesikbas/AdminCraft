package com.backend.infrastructure.persistence.platform.entity;

import com.backend.domain.entity.BaseEntity;
import com.backend.domain.enums.Language;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
    name = "platform_newsletter_subscriber_subscriptions",
    schema = "platform_management",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_platform_newsletter_subscriber_template_type",
            columnNames = { "subscriber_id", "template_key" }
        )
    }
)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, exclude = "subscriber")
@NoArgsConstructor
public class PlatformNewsletterSubscriberSubscription extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private PlatformNewsletterSubscriber subscriber;

    @Column(name = "template_key", nullable = false, length = 100)
    private String templateKey;

    @Column(name = "source", length = 120)
    private String source;

    @Column(name = "preferred_language", nullable = false, length = 10)
    private String preferredLanguage = Language.EN.name();

    @Column(name = "permission", nullable = false)
    private Boolean permission = Boolean.TRUE;
}
