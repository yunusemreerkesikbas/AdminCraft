package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;

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

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformMailMarketingService Tests")
class PlatformMailMarketingServiceTest {

    @Mock
    private PlatformEmailTemplateRepository templateRepository;

    @Mock
    private PlatformNewsletterSubscriberRepository subscriberRepository;

    @Mock
    private PlatformNewsletterSubscriberSubscriptionRepository subscriberSubscriptionRepository;

    @Mock
    private PlatformMailCampaignRepository campaignRepository;

    @Mock
    private PlatformMailOutboxRepository outboxRepository;

    @Mock
    private MailSenderPort mailSender;

    @Mock
    private EmailTemplateRendererPort templateRenderer;

    @Mock
    private TemplateVariableRenderer templateVariableRenderer;

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private PlatformTransactionManager platformTransactionManager;

    @InjectMocks
    private PlatformMailMarketingService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "platformDomain", "https://craftive.io");
    }

    @Test
    @DisplayName("subscribe should reject request when honeypot is filled")
    void subscribe_ShouldReject_WhenHoneypotIsFilled() {
        PlatformPublicNewsletterSubscribeCommand command = new PlatformPublicNewsletterSubscribeCommand(
                "test@example.com",
                "LANDING_NEWSLETTER",
                "NEWSLETTER_DEFAULT",
                "en",
                "spam",
                System.currentTimeMillis() - 5_000L);

        assertThatThrownBy(() -> service.subscribe(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("mail.marketing.newsletter.subscribe.blocked");

        verifyNoInteractions(subscriberRepository, subscriberSubscriptionRepository, templateRenderer, mailSender);
    }

    @Test
    @DisplayName("subscribe should reject request when submitted too quickly")
    void subscribe_ShouldReject_WhenSubmittedTooQuickly() {
        PlatformPublicNewsletterSubscribeCommand command = new PlatformPublicNewsletterSubscribeCommand(
                "test@example.com",
                "LANDING_NEWSLETTER",
                "NEWSLETTER_DEFAULT",
                "en",
                "",
                System.currentTimeMillis());

        assertThatThrownBy(() -> service.subscribe(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("mail.marketing.newsletter.subscribe.blocked");

        verifyNoInteractions(subscriberRepository, subscriberSubscriptionRepository, templateRenderer, mailSender);
    }

    @Test
    @DisplayName("subscribe should require confirmation for active subscriber on another template")
    void subscribe_ShouldRequireConfirmationForOtherTemplate() {
        PlatformNewsletterSubscriber subscriber = new PlatformNewsletterSubscriber();
        subscriber.setId(42L);
        subscriber.setEmail("admin@example.com");
        subscriber.setStatus(MailSubscriberStatus.ACTIVE);
        subscriber.setConfirmedAt(LocalDateTime.now().minusDays(1));
        subscriber.setUnsubscribeToken("unsubscribe-token");

        when(subscriberRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(subscriber));
        when(subscriberSubscriptionRepository.findBySubscriberAndTemplateKeyIgnoreCase(subscriber, "NEWSLETTER_DEFAULT"))
                .thenReturn(Optional.empty());
        when(subscriberRepository.save(any(PlatformNewsletterSubscriber.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(subscriberSubscriptionRepository.save(any(PlatformNewsletterSubscriberSubscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(templateRenderer.render(any(String.class), any(), any())).thenReturn("<html>confirm</html>");
        when(mailSender.send("admin@example.com", "Confirm Your Subscription", "<html>confirm</html>"))
                .thenReturn(EmailResult.success("msg-1"));

        String result = service.subscribe(new PlatformPublicNewsletterSubscribeCommand(
                "admin@example.com",
                "LANDING_NEWSLETTER",
                "NEWSLETTER_DEFAULT",
                "en",
                "",
                System.currentTimeMillis() - 5_000L));

        org.assertj.core.api.Assertions.assertThat(result)
                .isEqualTo("mail.marketing.newsletter.subscribe.sent");
        verify(subscriberRepository).save(argThat(saved ->
                saved.getStatus() == MailSubscriberStatus.ACTIVE
                        && saved.getConfirmToken() != null
                        && saved.getConfirmedAt() != null));
        verify(subscriberSubscriptionRepository).save(argThat(relation ->
                "NEWSLETTER_DEFAULT".equals(relation.getTemplateKey())
                        && Boolean.FALSE.equals(relation.getPermission())));
        verify(mailSender).send("admin@example.com", "Confirm Your Subscription", "<html>confirm</html>");
    }

    @Test
    @DisplayName("subscribe should be idempotent when newsletter subscription is already active")
    void subscribe_ShouldReturnAlreadyActiveWhenNewsletterSubscriptionExists() {
        PlatformNewsletterSubscriber subscriber = new PlatformNewsletterSubscriber();
        subscriber.setId(42L);
        subscriber.setEmail("admin@example.com");
        subscriber.setStatus(MailSubscriberStatus.ACTIVE);

        PlatformNewsletterSubscriberSubscription subscription = new PlatformNewsletterSubscriberSubscription();
        subscription.setId(7L);
        subscription.setSubscriber(subscriber);
        subscription.setTemplateKey("NEWSLETTER_DEFAULT");
        subscription.setPermission(Boolean.TRUE);
        subscription.setPreferredLanguage("EN");
        subscription.setSource("LANDING_NEWSLETTER");

        when(subscriberRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(subscriber));
        when(subscriberSubscriptionRepository.findBySubscriberAndTemplateKeyIgnoreCase(subscriber, "NEWSLETTER_DEFAULT"))
                .thenReturn(Optional.of(subscription));
        when(subscriberSubscriptionRepository.save(any(PlatformNewsletterSubscriberSubscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String result = service.subscribe(new PlatformPublicNewsletterSubscribeCommand(
                "admin@example.com",
                "LANDING_NEWSLETTER",
                "NEWSLETTER_DEFAULT",
                "en",
                "",
                System.currentTimeMillis() - 5_000L));

        // SEC-108: same response as new subscriber — no enumeration signal
        org.assertj.core.api.Assertions.assertThat(result)
                .isEqualTo("mail.marketing.newsletter.subscribe.sent");
        verify(mailSender, never()).send(any(), any(), any());
        verify(subscriberRepository, never()).save(any(PlatformNewsletterSubscriber.class));
    }

    @Test
    @DisplayName("subscribe should fail when confirmation email cannot be sent")
    void subscribe_ShouldFailWhenConfirmationEmailCannotBeSent() {
        when(subscriberRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.empty());
        when(subscriberRepository.save(any(PlatformNewsletterSubscriber.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(subscriberSubscriptionRepository.save(any(PlatformNewsletterSubscriberSubscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(templateRenderer.render(any(String.class), any(), any())).thenReturn("<html>confirm</html>");
        when(mailSender.send("test@example.com", "Confirm Your Subscription", "<html>confirm</html>"))
                .thenReturn(EmailResult.failure("smtp down"));

        assertThatThrownBy(() -> service.subscribe(new PlatformPublicNewsletterSubscribeCommand(
                "test@example.com",
                "LANDING_NEWSLETTER",
                "NEWSLETTER_DEFAULT",
                "en",
                "",
                System.currentTimeMillis() - 5_000L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("mail.marketing.newsletter.confirm.send.failed");
    }
}
