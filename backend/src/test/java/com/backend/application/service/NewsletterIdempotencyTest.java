package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import com.backend.application.dto.email.EmailResult;
import com.backend.application.dto.platform.PlatformPublicNewsletterSubscribeCommand;
import com.backend.application.service.mail.TemplateVariableRenderer;
import com.backend.domain.entity.PlatformNewsletterSubscriber;
import com.backend.domain.entity.PlatformNewsletterSubscriberSubscription;
import com.backend.domain.enums.MailSubscriberStatus;
import com.backend.domain.port.EmailTemplateRendererPort;
import com.backend.domain.port.MailSenderPort;
import com.backend.domain.repository.PlatformEmailTemplateRepository;
import com.backend.domain.repository.PlatformMailCampaignRepository;
import com.backend.domain.repository.PlatformMailOutboxRepository;
import com.backend.domain.repository.PlatformNewsletterSubscriberRepository;
import com.backend.domain.repository.PlatformNewsletterSubscriberSubscriptionRepository;
import com.backend.shared.common.SecurityHelper;

/**
 * SEC-108: newsletter idempotency — anti-enumeration and confirmation mail throttle.
 */
@ExtendWith(MockitoExtension.class)
class NewsletterIdempotencyTest {

    @Mock private PlatformEmailTemplateRepository templateRepository;
    @Mock private PlatformNewsletterSubscriberRepository subscriberRepository;
    @Mock private PlatformNewsletterSubscriberSubscriptionRepository subscriberSubscriptionRepository;
    @Mock private PlatformMailCampaignRepository campaignRepository;
    @Mock private PlatformMailOutboxRepository outboxRepository;
    @Mock private MailSenderPort mailSender;
    @Mock private EmailTemplateRendererPort templateRenderer;
    @Mock private TemplateVariableRenderer templateVariableRenderer;
    @Mock private SecurityHelper securityHelper;
    @Mock private PlatformTransactionManager platformTransactionManager;

    @InjectMocks
    private PlatformMailMarketingService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "platformDomain", "https://craftive.io");
    }

    @Test
    @DisplayName("SEC-108: already-active subscriber returns same response as new — no enumeration signal")
    void alreadyActive_returnsSameResponseAsNewSubscriber() {
        PlatformNewsletterSubscriber subscriber = activeSubscriber();
        PlatformNewsletterSubscriberSubscription subscription = activeSubscription(subscriber);

        when(subscriberRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(subscriber));
        when(subscriberSubscriptionRepository.findBySubscriberAndTemplateKeyIgnoreCase(subscriber, "NEWSLETTER_DEFAULT"))
                .thenReturn(Optional.of(subscription));
        when(subscriberSubscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String result = service.subscribe(command());

        assertThat(result).isEqualTo("mail.marketing.newsletter.subscribe.sent");
        verify(mailSender, never()).send(any(), any(), any());
    }

    @Test
    @DisplayName("SEC-108: PENDING within 5-min throttle window → confirmation mail suppressed")
    void pendingWithinThrottle_suppressesConfirmationMail() {
        PlatformNewsletterSubscriber subscriber = pendingSubscriber(LocalDateTime.now().minusMinutes(2));

        when(subscriberRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(subscriber));
        when(subscriberSubscriptionRepository.findBySubscriberAndTemplateKeyIgnoreCase(any(), any()))
                .thenReturn(Optional.empty());

        String result = service.subscribe(command());

        assertThat(result).isEqualTo("mail.marketing.newsletter.subscribe.sent");
        verify(mailSender, never()).send(any(), any(), any());
    }

    @Test
    @DisplayName("SEC-108: PENDING outside throttle window → new confirmation mail sent")
    void pendingOutsideThrottle_sendsNewConfirmationMail() {
        PlatformNewsletterSubscriber subscriber = pendingSubscriber(LocalDateTime.now().minusMinutes(10));

        when(subscriberRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(subscriber));
        when(subscriberSubscriptionRepository.findBySubscriberAndTemplateKeyIgnoreCase(any(), any()))
                .thenReturn(Optional.empty());
        when(subscriberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(subscriberSubscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(templateRenderer.render(any(), any(), any())).thenReturn("<html>confirm</html>");
        when(mailSender.send(any(), any(), any())).thenReturn(EmailResult.success("msg-1"));

        String result = service.subscribe(command());

        assertThat(result).isEqualTo("mail.marketing.newsletter.subscribe.sent");
        verify(mailSender).send(any(), any(), any());
    }

    private PlatformPublicNewsletterSubscribeCommand command() {
        return new PlatformPublicNewsletterSubscribeCommand(
                "user@example.com", "LANDING_NEWSLETTER", "NEWSLETTER_DEFAULT",
                "en", "", System.currentTimeMillis() - 5_000L);
    }

    private PlatformNewsletterSubscriber activeSubscriber() {
        PlatformNewsletterSubscriber s = new PlatformNewsletterSubscriber();
        s.setId(1L);
        s.setEmail("user@example.com");
        s.setStatus(MailSubscriberStatus.ACTIVE);
        s.setUnsubscribeToken("unsub-token");
        return s;
    }

    private PlatformNewsletterSubscriber pendingSubscriber(LocalDateTime updatedAt) {
        PlatformNewsletterSubscriber s = new PlatformNewsletterSubscriber();
        s.setId(2L);
        s.setEmail("user@example.com");
        s.setStatus(MailSubscriberStatus.PENDING_CONFIRMATION);
        s.setUpdatedAt(updatedAt);
        s.setUnsubscribeToken("unsub-token");
        return s;
    }

    private PlatformNewsletterSubscriberSubscription activeSubscription(PlatformNewsletterSubscriber subscriber) {
        PlatformNewsletterSubscriberSubscription sub = new PlatformNewsletterSubscriberSubscription();
        sub.setId(7L);
        sub.setSubscriber(subscriber);
        sub.setTemplateKey("NEWSLETTER_DEFAULT");
        sub.setPermission(Boolean.TRUE);
        return sub;
    }
}
