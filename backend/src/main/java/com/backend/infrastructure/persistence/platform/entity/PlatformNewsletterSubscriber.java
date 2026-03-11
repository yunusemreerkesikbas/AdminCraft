package com.backend.infrastructure.persistence.platform.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.backend.domain.entity.BaseEntity;
import com.backend.domain.enums.MailSubscriberStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "platform_newsletter_subscribers", schema = "platform_management")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, exclude = "subscriptions")
@NoArgsConstructor
public class PlatformNewsletterSubscriber extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private MailSubscriberStatus status = MailSubscriberStatus.PENDING_CONFIRMATION;

    @Column(name = "source", length = 120)
    private String source;

    @Column(name = "confirm_token", length = 255)
    private String confirmToken;

    @Column(name = "unsubscribe_token", length = 255)
    private String unsubscribeToken;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "unsubscribed_at")
    private LocalDateTime unsubscribedAt;

    @OneToMany(mappedBy = "subscriber")
    private List<PlatformNewsletterSubscriberSubscription> subscriptions = new ArrayList<>();
}
