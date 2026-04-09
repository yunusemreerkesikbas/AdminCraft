package com.backend.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.application.dto.email.EmailResult;
import com.backend.application.dto.mail.MailCampaignDto;
import com.backend.application.dto.mail.MailOutboxEntryDto;
import com.backend.application.dto.mail.MailSubscriberAdminDto;
import com.backend.application.dto.mail.MailSubscriberDto;
import com.backend.application.dto.mail.MailSubscriberSubscriptionDto;
import com.backend.application.dto.mail.MailTemplateDto;
import com.backend.application.dto.mail.MailTemplateTypeDetailDto;
import com.backend.application.dto.mail.MailTemplateTypeSummaryDto;
import com.backend.application.dto.platform.PlatformPublicNewsletterSubscribeCommand;
import com.backend.application.service.mail.TemplateVariableRenderer;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.MailCampaignStatus;
import com.backend.domain.enums.MailOutboxStatus;
import com.backend.domain.enums.MailSubscriberStatus;
import com.backend.domain.port.EmailTemplateRendererPort;
import com.backend.domain.port.MailSenderPort;
import com.backend.domain.entity.PlatformEmailTemplate;
import com.backend.domain.entity.PlatformMailCampaign;
import com.backend.domain.entity.PlatformMailOutbox;
import com.backend.domain.entity.PlatformNewsletterSubscriber;
import com.backend.domain.entity.PlatformNewsletterSubscriberSubscription;
import com.backend.domain.repository.PlatformEmailTemplateRepository;
import com.backend.domain.repository.PlatformMailCampaignRepository;
import com.backend.domain.repository.PlatformMailOutboxRepository;
import com.backend.domain.repository.PlatformNewsletterSubscriberRepository;
import com.backend.domain.repository.PlatformNewsletterSubscriberSubscriptionRepository;
import com.backend.shared.common.LogSanitizer;
import com.backend.shared.common.SecurityHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformMailMarketingService {

    private static final String LANGUAGE_TR = "TR";
    private static final String LANGUAGE_EN = "EN";
    private static final List<String> SUPPORTED_LANGUAGES = List.of(LANGUAGE_TR, LANGUAGE_EN);
    private static final String NEWSLETTER_DEFAULT = "NEWSLETTER_DEFAULT";
    private static final String VERSION_UPGRADE = "VERSION_UPGRADE";
    private static final String NEWSLETTER_SUBSCRIBE_SENT = "mail.marketing.newsletter.subscribe.sent";
    private static final String NEWSLETTER_SUBSCRIBE_ALREADY_ACTIVE = "mail.marketing.newsletter.subscribe.already.active";
    private static final String NEWSLETTER_CONFIRM_SEND_FAILED = "mail.marketing.newsletter.confirm.send.failed";
    private static final List<String> FIXED_TEMPLATE_TYPES = List.of(NEWSLETTER_DEFAULT, VERSION_UPGRADE);
    private static final List<String> SUBSCRIBABLE_TEMPLATE_TYPES = List.of(NEWSLETTER_DEFAULT, VERSION_UPGRADE);
    private static final long MIN_PUBLIC_SUBSCRIBE_FILL_DURATION_MS = 1000L;
    private static final long MAX_PUBLIC_SUBSCRIBE_AGE_MS = 3_600_000L;

    private final PlatformEmailTemplateRepository templateRepository;
    private final PlatformNewsletterSubscriberRepository subscriberRepository;
    private final PlatformNewsletterSubscriberSubscriptionRepository subscriberSubscriptionRepository;
    private final PlatformMailCampaignRepository campaignRepository;
    private final PlatformMailOutboxRepository outboxRepository;
    private final MailSenderPort mailSender;
    private final EmailTemplateRendererPort templateRenderer;
    private final TemplateVariableRenderer templateVariableRenderer;
    private final SecurityHelper securityHelper;
    @Qualifier("platformTransactionManager")
    private final PlatformTransactionManager platformTransactionManager;

    @Value("${app.platform-domain:craftive.io}")
    private String platformDomain;

    @Transactional("platformTransactionManager")
    public List<MailTemplateTypeSummaryDto> getTemplateTypes() {
        ensureAllFixedTemplateTypes();
        return FIXED_TEMPLATE_TYPES.stream()
            .map(this::toTemplateTypeSummary)
            .toList();
    }

    @Transactional("platformTransactionManager")
    public MailTemplateTypeDetailDto getTemplateTypeDetail(String templateTypeRaw) {
        String templateType = normalizeTemplateType(templateTypeRaw);
        ensureTemplateTypeTranslations(templateType);

        List<MailTemplateDto> translations = templateRepository
            .findByTemplateKeyIgnoreCaseOrderByLanguageAsc(templateType)
            .stream()
            .map(this::toTemplateDto)
            .toList();

        MailCampaignDto lastCampaign = campaignRepository
            .findTopByTemplateKeyOrderByCreatedAtDesc(templateType)
            .map(this::toCampaignDto)
            .orElse(null);

        Long subscriberCount = subscriberSubscriptionRepository.countByTemplateKeyIgnoreCase(templateType);
        return new MailTemplateTypeDetailDto(templateType, translations, lastCampaign, subscriberCount);
    }

    @Transactional("platformTransactionManager")
    public MailTemplateDto upsertTemplateTranslation(
        String templateTypeRaw,
        String languageRaw,
        String subject,
        String content,
        Boolean active
    ) {
        String templateType = normalizeTemplateType(templateTypeRaw);
        String language = normalizeLanguage(languageRaw);

        PlatformEmailTemplate template = templateRepository
            .findByTemplateKeyIgnoreCaseAndLanguageIgnoreCase(templateType, language)
            .orElseGet(() -> buildDefaultTemplate(templateType, language));

        if (subject != null) {
            template.setSubject(subject);
        }
        if (content != null) {
            template.setContent(content);
        }
        if (active != null) {
            template.setIsActive(active);
        }

        return toTemplateDto(templateRepository.save(template));
    }

    @Transactional(value = "platformTransactionManager", readOnly = true)
    public List<MailSubscriberDto> getSubscribersByTemplateType(String templateTypeRaw) {
        String templateType = normalizeTemplateType(templateTypeRaw);
        return subscriberSubscriptionRepository
            .findByTemplateKeyIgnoreCaseOrderBySubscriberCreatedAtDesc(templateType)
            .stream()
            .map(this::toSubscriberDto)
            .toList();
    }

    @Transactional(value = "platformTransactionManager", readOnly = true)
    public Page<MailSubscriberAdminDto> getSubscriberPage(
        Pageable pageable,
        String search,
        MailSubscriberStatus status,
        String templateTypeRaw,
        Boolean permission
    ) {
        String templateType = normalizeOptionalTemplateType(templateTypeRaw);
        Page<PlatformNewsletterSubscriber> page = subscriberRepository.searchSubscribers(
            search,
            status,
            templateType,
            permission,
            pageable
        );

        List<Long> subscriberIds = page.getContent().stream()
            .map(PlatformNewsletterSubscriber::getId)
            .toList();
        Map<Long, List<PlatformNewsletterSubscriberSubscription>> relationMap = subscriberIds.isEmpty()
            ? Map.of()
            : subscriberSubscriptionRepository.findBySubscriberIdInOrderByTemplateKeyAsc(subscriberIds)
                .stream()
                .collect(Collectors.groupingBy(
                    relation -> relation.getSubscriber().getId(),
                    LinkedHashMap::new,
                    Collectors.toList()
                ));

        List<MailSubscriberAdminDto> content = page.getContent().stream()
            .map(subscriber -> toSubscriberAdminDto(
                subscriber,
                relationMap.getOrDefault(subscriber.getId(), List.of())
            ))
            .toList();
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Transactional(value = "platformTransactionManager", readOnly = true)
    public MailSubscriberAdminDto getSubscriber(Long subscriberId) {
        PlatformNewsletterSubscriber subscriber = subscriberRepository.findById(subscriberId)
            .orElseThrow(() -> new IllegalArgumentException("mail.marketing.subscriber.not.found"));
        List<PlatformNewsletterSubscriberSubscription> relations =
            subscriberSubscriptionRepository.findBySubscriberOrderByTemplateKeyAsc(subscriber);
        return toSubscriberAdminDto(subscriber, relations);
    }

    @Transactional("platformTransactionManager")
    public MailSubscriberAdminDto createSubscriber(
        String email,
        MailSubscriberStatus status,
        List<MailSubscriberSubscriptionDto> subscriptions
    ) {
        String normalizedEmail = normalizeEmail(email);
        if (subscriberRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("mail.marketing.subscriber.email.exists");
        }

        PlatformNewsletterSubscriber subscriber = new PlatformNewsletterSubscriber();
        subscriber.setEmail(normalizedEmail);
        subscriber.setConfirmToken(null);
        subscriber.setUnsubscribeToken(UUID.randomUUID().toString());
        applyAdminStatus(subscriber, normalizeAdminStatus(status, MailSubscriberStatus.ACTIVE));
        PlatformNewsletterSubscriber saved = subscriberRepository.save(subscriber);

        List<PlatformNewsletterSubscriberSubscription> savedRelations = syncTemplateSubscriptions(saved, subscriptions);
        return toSubscriberAdminDto(saved, savedRelations);
    }

    @Transactional("platformTransactionManager")
    public MailSubscriberAdminDto updateSubscriber(
        Long subscriberId,
        String email,
        MailSubscriberStatus status,
        List<MailSubscriberSubscriptionDto> subscriptions
    ) {
        PlatformNewsletterSubscriber subscriber = subscriberRepository.findById(subscriberId)
            .orElseThrow(() -> new IllegalArgumentException("mail.marketing.subscriber.not.found"));
        if (subscriber.getUnsubscribeToken() == null || subscriber.getUnsubscribeToken().isBlank()) {
            subscriber.setUnsubscribeToken(UUID.randomUUID().toString());
        }
        if (email != null && !email.isBlank()) {
            String normalizedEmail = normalizeEmail(email);
            if (!normalizedEmail.equalsIgnoreCase(subscriber.getEmail())
                    && subscriberRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                throw new IllegalArgumentException("mail.marketing.subscriber.email.exists");
            }
            subscriber.setEmail(normalizedEmail);
        }
        if (status != null) {
            applyAdminStatus(subscriber, normalizeAdminStatus(status, subscriber.getStatus()));
            subscriberRepository.save(subscriber);
        }

        List<PlatformNewsletterSubscriberSubscription> savedRelations = syncTemplateSubscriptions(subscriber, subscriptions);
        return toSubscriberAdminDto(subscriber, savedRelations);
    }

    @Transactional("platformTransactionManager")
    public void deleteSubscriber(Long subscriberId) {
        PlatformNewsletterSubscriber subscriber = subscriberRepository.findById(subscriberId)
            .orElseThrow(() -> new IllegalArgumentException("mail.marketing.subscriber.not.found"));
        subscriber.setStatus(MailSubscriberStatus.UNSUBSCRIBED);
        subscriber.setConfirmToken(null);
        subscriber.setUnsubscribedAt(LocalDateTime.now());
        subscriberRepository.save(subscriber);

        List<PlatformNewsletterSubscriberSubscription> relations =
            subscriberSubscriptionRepository.findBySubscriberOrderByTemplateKeyAsc(subscriber);
        if (!relations.isEmpty()) {
            relations.forEach(relation -> relation.setPermission(Boolean.FALSE));
            subscriberSubscriptionRepository.saveAll(relations);
        }
    }

    @Transactional("platformTransactionManager")
    public String subscribe(PlatformPublicNewsletterSubscribeCommand command) {
        validatePublicSubscribeRequest(command);
        String templateType = normalizeTemplateType(command.templateType());
        if (!SUBSCRIBABLE_TEMPLATE_TYPES.contains(templateType)) {
            throw new IllegalArgumentException("mail.marketing.template.type.invalid");
        }

        String normalizedEmail = normalizeEmail(command.email());
        PlatformNewsletterSubscriber subscriber = subscriberRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseGet(PlatformNewsletterSubscriber::new);
        PlatformNewsletterSubscriberSubscription existingSubscription = subscriber.getId() == null
            ? null
            : subscriberSubscriptionRepository.findBySubscriberAndTemplateKeyIgnoreCase(subscriber, templateType).orElse(null);

        String preferredLang = LANGUAGE_TR.equalsIgnoreCase(command.locale()) ? LANGUAGE_TR : LANGUAGE_EN;

        subscriber.setEmail(normalizedEmail);
        boolean alreadySubscribed = subscriber.getStatus() == MailSubscriberStatus.ACTIVE
            && existingSubscription != null
            && Boolean.TRUE.equals(existingSubscription.getPermission());
        if (alreadySubscribed) {
            upsertTemplateSubscription(subscriber, templateType, command.source(), preferredLang, Boolean.TRUE);
            return NEWSLETTER_SUBSCRIBE_ALREADY_ACTIVE;
        }

        boolean keepSubscriberActive = subscriber.getStatus() == MailSubscriberStatus.ACTIVE;
        if (!keepSubscriberActive) {
            subscriber.setStatus(MailSubscriberStatus.PENDING_CONFIRMATION);
            subscriber.setConfirmedAt(null);
        }
        subscriber.setConfirmToken(UUID.randomUUID().toString());
        if (subscriber.getUnsubscribeToken() == null || subscriber.getUnsubscribeToken().isBlank()) {
            subscriber.setUnsubscribeToken(UUID.randomUUID().toString());
        }
        subscriber.setUnsubscribedAt(null);
        PlatformNewsletterSubscriber savedSubscriber = subscriberRepository.save(subscriber);
        upsertTemplateSubscription(savedSubscriber, templateType, command.source(), preferredLang, Boolean.FALSE);

        String confirmLink = buildPlatformUrl("/newsletter/confirm?token=" + savedSubscriber.getConfirmToken());
        Language emailLang = LANGUAGE_TR.equals(preferredLang) ? Language.TR : Language.EN;
        String templateName = "email/newsletter-confirm-" + emailLang.name().toLowerCase();
        String subject = Language.TR == emailLang ? "Aboneliğinizi Onaylayın" : "Confirm Your Subscription";
        log.info("[MAIL] newsletter-confirm dispatch requested | recipient={}",
            LogSanitizer.maskEmail(savedSubscriber.getEmail()));
        String html = templateRenderer.render(templateName, Map.of("confirmLink", confirmLink), emailLang);
        EmailResult result = mailSender.send(savedSubscriber.getEmail(), subject, html);
        if (!result.isSuccess()) {
            throw new IllegalStateException(NEWSLETTER_CONFIRM_SEND_FAILED);
        }
        return NEWSLETTER_SUBSCRIBE_SENT;
    }

    private void validatePublicSubscribeRequest(PlatformPublicNewsletterSubscribeCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("mail.marketing.newsletter.subscribe.blocked");
        }
        if (command.honeypot() != null && !command.honeypot().isBlank()) {
            throw new IllegalArgumentException("mail.marketing.newsletter.subscribe.blocked");
        }
        Long formStartedAt = command.formStartedAt();
        if (formStartedAt == null) {
            throw new IllegalArgumentException("mail.marketing.newsletter.subscribe.blocked");
        }
        long elapsedMs = System.currentTimeMillis() - formStartedAt;
        if (elapsedMs < MIN_PUBLIC_SUBSCRIBE_FILL_DURATION_MS || elapsedMs > MAX_PUBLIC_SUBSCRIBE_AGE_MS) {
            throw new IllegalArgumentException("mail.marketing.newsletter.subscribe.blocked");
        }
    }

    @Transactional("platformTransactionManager")
    public void autoSubscribeTenantAdmin(String email, String preferredLanguage) {
        if (email == null || email.isBlank()) {
            return;
        }
        String normalizedEmail = email.trim().toLowerCase();
        String normalizedLang = normalizePreferredLanguage(preferredLanguage);

        PlatformNewsletterSubscriber subscriber = subscriberRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(PlatformNewsletterSubscriber::new);

        subscriber.setEmail(normalizedEmail);
        if (subscriber.getStatus() != MailSubscriberStatus.ACTIVE) {
            subscriber.setStatus(MailSubscriberStatus.ACTIVE);
            subscriber.setConfirmedAt(java.time.LocalDateTime.now());
            subscriber.setUnsubscribedAt(null);
        }
        subscriber.setConfirmToken(null);
        if (subscriber.getUnsubscribeToken() == null || subscriber.getUnsubscribeToken().isBlank()) {
            subscriber.setUnsubscribeToken(UUID.randomUUID().toString());
        }
        PlatformNewsletterSubscriber saved = subscriberRepository.save(subscriber);
        upsertTemplateSubscription(saved, VERSION_UPGRADE, "TENANT_ONBOARDING", normalizedLang, Boolean.TRUE);
    }

    public void sendDemoRequestConfirmation(String email, String fullName, String locale) {
        Language lang = LANGUAGE_TR.equalsIgnoreCase(locale) ? Language.TR : Language.EN;
        String templateName = "email/demo-request-confirmation-" + lang.name().toLowerCase();
        String subject = Language.TR == lang ? "Talebiniz Alındı" : "We Received Your Request";
        Map<String, Object> vars = Map.of("name", fullName != null ? fullName : email);
        log.info("[MAIL] demo-request-confirm dispatch requested | recipient={}", LogSanitizer.maskEmail(email));
        String html = templateRenderer.render(templateName, vars, lang);
        EmailResult result = mailSender.send(email, subject, html);
        if (!result.isSuccess()) {
            log.warn("[MAIL] demo-request-confirm send failed | recipient={} | reason={}",
                LogSanitizer.maskEmail(email),
                LogSanitizer.sanitizeForLog(result.getErrorMessage()));
        }
    }

    @Transactional("platformTransactionManager")
    public void confirm(String token) {
        PlatformNewsletterSubscriber subscriber = subscriberRepository.findByConfirmToken(token)
            .orElseThrow(() -> new IllegalArgumentException("mail.marketing.newsletter.confirm.token.invalid"));
        subscriber.setStatus(MailSubscriberStatus.ACTIVE);
        subscriber.setConfirmedAt(LocalDateTime.now());
        subscriber.setConfirmToken(null);
        subscriberRepository.save(subscriber);

        List<PlatformNewsletterSubscriberSubscription> relations =
            subscriberSubscriptionRepository.findBySubscriberOrderByTemplateKeyAsc(subscriber);
        if (!relations.isEmpty()) {
            boolean changed = false;
            for (PlatformNewsletterSubscriberSubscription relation : relations) {
                if (!Boolean.TRUE.equals(relation.getPermission())) {
                    relation.setPermission(Boolean.TRUE);
                    changed = true;
                }
            }
            if (changed) {
                subscriberSubscriptionRepository.saveAll(relations);
            }
        }
    }

    @Transactional("platformTransactionManager")
    public void unsubscribe(String token) {
        PlatformNewsletterSubscriber subscriber = subscriberRepository.findByUnsubscribeToken(token)
            .orElseThrow(() -> new IllegalArgumentException("mail.marketing.newsletter.unsubscribe.token.invalid"));
        subscriber.setStatus(MailSubscriberStatus.UNSUBSCRIBED);
        subscriber.setUnsubscribedAt(LocalDateTime.now());
        subscriberRepository.save(subscriber);
    }

    public MailCampaignDto sendCampaign(String templateTypeRaw) {
        String templateType = normalizeTemplateType(templateTypeRaw);
        TransactionTemplate tx = new TransactionTemplate(platformTransactionManager);

        record CampaignSetup(Long campaignId, List<Long> outboxIds) {
        }
        CampaignSetup setup = tx.execute(status -> {
            List<PlatformEmailTemplate> templateTranslations =
                templateRepository.findByTemplateKeyIgnoreCaseOrderByLanguageAsc(templateType);

            PlatformEmailTemplate primaryTemplate = templateTranslations.stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsActive()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("mail.marketing.template.not.active"));

            List<PlatformNewsletterSubscriberSubscription> subscriptions = subscriberSubscriptionRepository
                .findByTemplateKeyIgnoreCaseAndPermissionTrueAndSubscriberStatus(templateType, MailSubscriberStatus.ACTIVE);

            PlatformMailCampaign campaign = new PlatformMailCampaign();
            campaign.setTemplate(primaryTemplate);
            campaign.setSubject(primaryTemplate.getSubject());
            campaign.setContent(primaryTemplate.getContent());
            campaign.setStatus(MailCampaignStatus.SENDING);
            campaign.setTotalCount(subscriptions.size());
            campaign.setCreatedByEmail(resolveCurrentUserEmail());
            campaign = campaignRepository.save(campaign);

            List<Long> outboxIds = new ArrayList<>();
            for (PlatformNewsletterSubscriberSubscription subscription : subscriptions) {
                PlatformNewsletterSubscriber subscriber = subscription.getSubscriber();
                PlatformEmailTemplate resolvedTemplate = resolveTemplateForLanguage(
                templateTranslations,
                normalizePreferredLanguage(subscription.getPreferredLanguage()),
                primaryTemplate
                );
                PlatformMailOutbox outbox = new PlatformMailOutbox();
                outbox.setCampaign(campaign);
                outbox.setToEmail(subscriber.getEmail());
                outbox.setSubject(render(resolvedTemplate.getSubject(), subscriber));
                outbox.setContent(render(resolvedTemplate.getContent(), subscriber));
                outbox.setStatus(MailOutboxStatus.PENDING);
                outbox = outboxRepository.save(outbox);
                outboxIds.add(outbox.getId());
            }
            return new CampaignSetup(campaign.getId(), outboxIds);
        });

        int sent = 0;
        int failed = 0;
        for (Long outboxId : setup.outboxIds()) {
            boolean success = Boolean.TRUE.equals(tx.execute(status -> {
                PlatformMailOutbox outbox = outboxRepository.findById(outboxId)
                    .orElseThrow(() -> new IllegalStateException("mail.marketing.outbox.not.found"));
                EmailResult result = mailSender.send(outbox.getToEmail(), outbox.getSubject(), outbox.getContent());
                if (result.isSuccess()) {
                    outbox.setStatus(MailOutboxStatus.SENT);
                    outbox.setProviderMessageId(result.getMessageId());
                } else {
                    outbox.setStatus(MailOutboxStatus.FAILED);
                    outbox.setErrorMessage(LogSanitizer.sanitizeForLog(result.getErrorMessage()));
                }
                outboxRepository.save(outbox);
                return result.isSuccess();
            }));
            if (success) {
                sent++;
            } else {
                failed++;
            }
        }

        int finalSent = sent;
        int finalFailed = failed;
        return tx.execute(status -> {
            PlatformMailCampaign campaign = campaignRepository.findById(setup.campaignId())
                .orElseThrow(() -> new IllegalStateException("mail.marketing.campaign.not.found"));
            campaign.setSentCount(finalSent);
            campaign.setFailedCount(finalFailed);
            campaign.setStatus(finalFailed > 0 ? MailCampaignStatus.COMPLETED_WITH_ERRORS : MailCampaignStatus.COMPLETED);
            return toCampaignDto(campaignRepository.save(campaign));
        });
    }

    @Transactional(value = "platformTransactionManager", readOnly = true)
    public MailCampaignDto getCampaign(Long campaignId) {
        PlatformMailCampaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new IllegalArgumentException("mail.marketing.campaign.not.found"));
        return toCampaignDto(campaign);
    }

    @Transactional(value = "platformTransactionManager", readOnly = true)
    public List<MailCampaignDto> getCampaignList(String templateTypeRaw, int size) {
        String templateType = normalizeTemplateType(templateTypeRaw);
        return campaignRepository.findRecentByTemplateKey(templateType, size)
            .stream()
            .map(this::toCampaignDto)
            .toList();
    }

    @Transactional(value = "platformTransactionManager", readOnly = true)
    public List<MailOutboxEntryDto> getOutboxEntries(Long campaignId) {
        campaignRepository.findById(campaignId)
            .orElseThrow(() -> new IllegalArgumentException("mail.marketing.campaign.not.found"));
        return outboxRepository.findByCampaignIdAndStatusIn(
                campaignId, List.of(MailOutboxStatus.FAILED, MailOutboxStatus.PENDING, MailOutboxStatus.PROCESSING))
            .stream()
            .map(o -> new MailOutboxEntryDto(o.getId(), o.getToEmail(), o.getStatus(), o.getErrorMessage(), o.getProviderMessageId()))
            .toList();
    }

    @Transactional(value = "platformTransactionManager", readOnly = true)
    public String exportSubscribersCsv(String templateTypeRaw) {
        String templateType = normalizeOptionalTemplateType(templateTypeRaw);
        List<PlatformNewsletterSubscriberSubscription> subscriptions = templateType != null
            ? subscriberSubscriptionRepository.findByTemplateKeyIgnoreCaseOrderBySubscriberCreatedAtDesc(templateType)
            : subscriberSubscriptionRepository.findAllByOrderBySubscriberCreatedAtDesc();

        StringBuilder csv = new StringBuilder("email,status,templateType,preferredLanguage,permission,createdAt\n");
        for (PlatformNewsletterSubscriberSubscription sub : subscriptions) {
            PlatformNewsletterSubscriber s = sub.getSubscriber();
            csv.append(csvEscape(s.getEmail())).append(',')
               .append(s.getStatus()).append(',')
               .append(csvEscape(sub.getTemplateKey())).append(',')
               .append(csvEscape(normalizePreferredLanguage(sub.getPreferredLanguage()))).append(',')
               .append(Boolean.TRUE.equals(sub.getPermission())).append(',')
               .append(s.getCreatedAt() != null ? s.getCreatedAt().toString() : "")
               .append('\n');
        }
        return csv.toString();
    }

    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void ensureAllFixedTemplateTypes() {
        for (String templateType : FIXED_TEMPLATE_TYPES) {
            ensureTemplateTypeTranslations(templateType);
        }
    }

    private void ensureTemplateTypeTranslations(String templateType) {
        for (String language : SUPPORTED_LANGUAGES) {
            templateRepository.findByTemplateKeyIgnoreCaseAndLanguageIgnoreCase(templateType, language)
                .orElseGet(() -> templateRepository.save(buildDefaultTemplate(templateType, language)));
        }
    }

    private PlatformNewsletterSubscriberSubscription upsertTemplateSubscription(
        PlatformNewsletterSubscriber subscriber,
        String templateType,
        String source,
        String preferredLanguage,
        Boolean permission
    ) {
        PlatformNewsletterSubscriberSubscription relation = subscriberSubscriptionRepository
            .findBySubscriberAndTemplateKeyIgnoreCase(subscriber, templateType)
            .orElseGet(() -> {
                PlatformNewsletterSubscriberSubscription created = new PlatformNewsletterSubscriberSubscription();
                created.setSubscriber(subscriber);
                created.setTemplateKey(templateType);
                return created;
            });

        relation.setSource(source);
        relation.setPreferredLanguage(preferredLanguage);
        relation.setPermission(permission);
        return subscriberSubscriptionRepository.save(relation);
    }

    private List<PlatformNewsletterSubscriberSubscription> syncTemplateSubscriptions(
        PlatformNewsletterSubscriber subscriber,
        List<MailSubscriberSubscriptionDto> subscriptions
    ) {
        if (subscriptions == null || subscriptions.isEmpty()) {
            throw new IllegalArgumentException("mail.marketing.subscriber.subscription.required");
        }

        Map<String, MailSubscriberSubscriptionDto> normalized = new LinkedHashMap<>();
        for (MailSubscriberSubscriptionDto subscription : subscriptions) {
            if (subscription == null) {
                continue;
            }
            String templateType = normalizeTemplateType(subscription.templateType());
            if (normalized.putIfAbsent(templateType, subscription) != null) {
                throw new IllegalArgumentException("mail.marketing.subscriber.subscription.duplicate.template");
            }
        }
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("mail.marketing.subscriber.subscription.required");
        }

        List<PlatformNewsletterSubscriberSubscription> existing =
            subscriberSubscriptionRepository.findBySubscriberOrderByTemplateKeyAsc(subscriber);
        Map<String, PlatformNewsletterSubscriberSubscription> existingByType = existing.stream()
            .collect(Collectors.toMap(
                relation -> relation.getTemplateKey().toUpperCase(),
                relation -> relation,
                (left, right) -> left,
                LinkedHashMap::new
            ));

        List<PlatformNewsletterSubscriberSubscription> toDelete = new ArrayList<>();
        for (PlatformNewsletterSubscriberSubscription relation : existing) {
            if (!normalized.containsKey(relation.getTemplateKey().toUpperCase())) {
                toDelete.add(relation);
            }
        }
        if (!toDelete.isEmpty()) {
            subscriberSubscriptionRepository.deleteAll(toDelete);
        }

        List<PlatformNewsletterSubscriberSubscription> toSave = new ArrayList<>();
        for (Map.Entry<String, MailSubscriberSubscriptionDto> entry : normalized.entrySet()) {
            String templateType = entry.getKey();
            MailSubscriberSubscriptionDto incoming = entry.getValue();
            PlatformNewsletterSubscriberSubscription relation = existingByType.get(templateType);
            if (relation == null) {
                relation = new PlatformNewsletterSubscriberSubscription();
                relation.setSubscriber(subscriber);
                relation.setTemplateKey(templateType);
            }
            relation.setSource(normalizeSource(incoming.source()));
            relation.setPreferredLanguage(normalizePreferredLanguage(incoming.preferredLanguage()));
            relation.setPermission(normalizePermission(incoming.permission()));
            toSave.add(relation);
        }

        List<PlatformNewsletterSubscriberSubscription> saved = subscriberSubscriptionRepository.saveAll(toSave);
        String firstSource = saved.stream()
            .map(PlatformNewsletterSubscriberSubscription::getSource)
            .filter(source -> source != null && !source.isBlank())
            .findFirst()
            .orElse(null);
        subscriber.setSource(firstSource);
        subscriberRepository.save(subscriber);
        return saved.stream()
            .sorted((left, right) -> left.getTemplateKey().compareToIgnoreCase(right.getTemplateKey()))
            .toList();
    }

    private MailTemplateTypeSummaryDto toTemplateTypeSummary(String templateType) {
        List<PlatformEmailTemplate> templates = templateRepository.findByTemplateKeyIgnoreCaseOrderByLanguageAsc(templateType);
        boolean active = templates.stream().anyMatch(t -> Boolean.TRUE.equals(t.getIsActive()));
        List<String> languages = templates.stream()
            .map(PlatformEmailTemplate::getLanguage)
            .map(String::toUpperCase)
            .distinct()
            .toList();
        Long subscriberCount = subscriberSubscriptionRepository.countByTemplateKeyIgnoreCase(templateType);
        LocalDateTime lastCampaignAt = campaignRepository
            .findTopByTemplateKeyOrderByCreatedAtDesc(templateType)
            .map(PlatformMailCampaign::getCreatedAt)
            .orElse(null);

        return new MailTemplateTypeSummaryDto(templateType, active, languages, subscriberCount, lastCampaignAt);
    }

    private PlatformEmailTemplate buildDefaultTemplate(String templateType, String language) {
        PlatformEmailTemplate template = new PlatformEmailTemplate();
        template.setTemplateKey(templateType);
        template.setLanguage(language);
        template.setSubject(defaultSubject(templateType, language));
        template.setContent(defaultContent(templateType, language));
        template.setIsActive(true);
        return template;
    }

    private String normalizeTemplateType(String templateTypeRaw) {
        if (templateTypeRaw == null || templateTypeRaw.isBlank()) {
            throw new IllegalArgumentException("mail.marketing.template.type.required");
        }
        String normalized = templateTypeRaw.trim().toUpperCase();
        if (!FIXED_TEMPLATE_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("mail.marketing.template.type.invalid");
        }
        return normalized;
    }

    private String normalizeOptionalTemplateType(String templateTypeRaw) {
        if (templateTypeRaw == null || templateTypeRaw.isBlank()) {
            return null;
        }
        return normalizeTemplateType(templateTypeRaw);
    }

    private String normalizeEmail(String emailRaw) {
        if (emailRaw == null || emailRaw.isBlank()) {
            throw new IllegalArgumentException("mail.marketing.subscriber.email.required");
        }
        return emailRaw.trim().toLowerCase();
    }

    private String normalizeSource(String sourceRaw) {
        if (sourceRaw == null) {
            return null;
        }
        String normalized = sourceRaw.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private Boolean normalizePermission(Boolean permission) {
        return permission == null ? Boolean.TRUE : permission;
    }

    private MailSubscriberStatus normalizeAdminStatus(
        MailSubscriberStatus status,
        MailSubscriberStatus fallback
    ) {
        MailSubscriberStatus effective = status == null ? fallback : status;
        if (effective == MailSubscriberStatus.ACTIVE || effective == MailSubscriberStatus.UNSUBSCRIBED) {
            return effective;
        }
        throw new IllegalArgumentException("mail.marketing.subscriber.status.invalid");
    }

    private void applyAdminStatus(PlatformNewsletterSubscriber subscriber, MailSubscriberStatus status) {
        subscriber.setStatus(status);
        subscriber.setConfirmToken(null);
        if (status == MailSubscriberStatus.ACTIVE) {
            if (subscriber.getConfirmedAt() == null) {
                subscriber.setConfirmedAt(LocalDateTime.now());
            }
            subscriber.setUnsubscribedAt(null);
            return;
        }
        subscriber.setUnsubscribedAt(LocalDateTime.now());
    }

    private String normalizeLanguage(String languageRaw) {
        if (languageRaw == null || languageRaw.isBlank()) {
            throw new IllegalArgumentException("mail.marketing.template.language.required");
        }
        String normalized = languageRaw.trim().toUpperCase();
        if (!SUPPORTED_LANGUAGES.contains(normalized)) {
            throw new IllegalArgumentException("mail.marketing.template.language.invalid");
        }
        return normalized;
    }

    private String normalizePreferredLanguage(String languageRaw) {
        if (languageRaw == null || languageRaw.isBlank()) {
            return LANGUAGE_EN;
        }
        return LANGUAGE_TR.equalsIgnoreCase(languageRaw) ? LANGUAGE_TR : LANGUAGE_EN;
    }

    private PlatformEmailTemplate resolveTemplateForLanguage(
        List<PlatformEmailTemplate> translations,
        String preferredLanguage,
        PlatformEmailTemplate fallbackTemplate
    ) {
        for (PlatformEmailTemplate template : translations) {
            if (preferredLanguage.equalsIgnoreCase(template.getLanguage())
                && Boolean.TRUE.equals(template.getIsActive())) {
                return template;
            }
        }
        for (PlatformEmailTemplate template : translations) {
            if (LANGUAGE_EN.equalsIgnoreCase(template.getLanguage())
                && Boolean.TRUE.equals(template.getIsActive())) {
                return template;
            }
        }
        return fallbackTemplate;
    }

    private String defaultSubject(String templateType, String language) {
        if (VERSION_UPGRADE.equals(templateType)) {
            return LANGUAGE_TR.equals(language) ? "Versiyon Güncellemesi" : "Version Upgrade";
        }
        return LANGUAGE_TR.equals(language) ? "Bülten Bilgilendirmesi" : "Newsletter Update";
    }

    private String defaultContent(String templateType, String language) {
        if (VERSION_UPGRADE.equals(templateType)) {
            return LANGUAGE_TR.equals(language)
                ? "Merhaba {{name}},\n\nYeni versiyon yayınlandı: {{content}}\n\nİptal: {{unsubscribeUrl}}"
                : "Hello {{name}},\n\nA new version is available: {{content}}\n\nUnsubscribe: {{unsubscribeUrl}}";
        }
        return LANGUAGE_TR.equals(language)
            ? "Merhaba {{name}},\n\n{{content}}\n\nİptal: {{unsubscribeUrl}}"
            : "Hello {{name}},\n\n{{content}}\n\nUnsubscribe: {{unsubscribeUrl}}";
    }

    private String render(String template, PlatformNewsletterSubscriber subscriber) {
        String name = subscriber.getEmail();
        int at = name.indexOf('@');
        if (at > 0) {
            name = name.substring(0, at);
        }
        String unsubscribeUrl = buildPlatformUrl("/newsletter/unsubscribe?token=" + subscriber.getUnsubscribeToken());
        return templateVariableRenderer.render(template, Map.of(
            "name", name,
            "email", subscriber.getEmail(),
            "unsubscribeUrl", unsubscribeUrl
        ));
    }

    private String buildPlatformUrl(String path) {
        String host = platformDomain;
        if (!host.startsWith("http://") && !host.startsWith("https://")) {
            host = "https://" + host;
        }
        return host + path;
    }

    private String resolveCurrentUserEmail() {
        if (!securityHelper.isAuthenticated()) {
            return "system";
        }
        return securityHelper.getCurrentUserEmail();
    }

    private MailTemplateDto toTemplateDto(PlatformEmailTemplate template) {
        return new MailTemplateDto(
            template.getId(),
            template.getTemplateKey(),
            template.getLanguage(),
            template.getSubject(),
            template.getContent(),
            template.getIsActive()
        );
    }

    private MailCampaignDto toCampaignDto(PlatformMailCampaign campaign) {
        return new MailCampaignDto(
            campaign.getId(),
            campaign.getStatus(),
            campaign.getTotalCount(),
            campaign.getSentCount(),
            campaign.getFailedCount(),
            campaign.getCreatedAt()
        );
    }

    private MailSubscriberAdminDto toSubscriberAdminDto(
        PlatformNewsletterSubscriber subscriber,
        List<PlatformNewsletterSubscriberSubscription> relations
    ) {
        List<MailSubscriberSubscriptionDto> subscriptions = relations.stream()
            .sorted((left, right) -> left.getTemplateKey().compareToIgnoreCase(right.getTemplateKey()))
            .map(this::toSubscriptionDto)
            .toList();
        return new MailSubscriberAdminDto(
            subscriber.getId(),
            subscriber.getEmail(),
            subscriber.getStatus(),
            subscriptions,
            subscriber.getCreatedAt(),
            subscriber.getConfirmedAt(),
            subscriber.getUnsubscribedAt()
        );
    }

    private MailSubscriberSubscriptionDto toSubscriptionDto(PlatformNewsletterSubscriberSubscription relation) {
        return new MailSubscriberSubscriptionDto(
            relation.getTemplateKey(),
            relation.getSource(),
            normalizePreferredLanguage(relation.getPreferredLanguage()),
            normalizePermission(relation.getPermission())
        );
    }

    private MailSubscriberDto toSubscriberDto(PlatformNewsletterSubscriberSubscription subscription) {
        PlatformNewsletterSubscriber subscriber = subscription.getSubscriber();
        return new MailSubscriberDto(
            subscriber.getId(),
            subscriber.getEmail(),
            subscriber.getStatus(),
            subscription.getSource(),
            normalizePreferredLanguage(subscription.getPreferredLanguage()),
            normalizePermission(subscription.getPermission()),
            subscriber.getCreatedAt(),
            subscriber.getConfirmedAt(),
            subscriber.getUnsubscribedAt()
        );
    }
}
