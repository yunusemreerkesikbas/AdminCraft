package com.backend.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.email.EmailResult;
import com.backend.application.dto.mail.MailCampaignDto;
import com.backend.application.dto.mail.MailSubscriberAdminDto;
import com.backend.application.dto.mail.MailSubscriberDto;
import com.backend.application.dto.mail.MailSubscriberSubscriptionDto;
import com.backend.application.dto.mail.MailTemplateDto;
import com.backend.application.dto.mail.MailTemplateTypeDetailDto;
import com.backend.application.dto.mail.MailTemplateTypeSummaryDto;
import com.backend.application.service.mail.TemplateVariableRenderer;
import com.backend.domain.enums.MailCampaignStatus;
import com.backend.domain.enums.MailOutboxStatus;
import com.backend.domain.enums.MailSubscriberStatus;
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
import com.backend.shared.common.SecurityHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlatformMailMarketingService {

    private static final String LANGUAGE_TR = "TR";
    private static final String LANGUAGE_EN = "EN";
    private static final List<String> SUPPORTED_LANGUAGES = List.of(LANGUAGE_TR, LANGUAGE_EN);
    private static final String NEWSLETTER_DEFAULT = "NEWSLETTER_DEFAULT";
    private static final String VERSION_UPGRADE = "VERSION_UPGRADE";
    private static final List<String> FIXED_TEMPLATE_TYPES = List.of(NEWSLETTER_DEFAULT, VERSION_UPGRADE);

    private final PlatformEmailTemplateRepository templateRepository;
    private final PlatformNewsletterSubscriberRepository subscriberRepository;
    private final PlatformNewsletterSubscriberSubscriptionRepository subscriberSubscriptionRepository;
    private final PlatformMailCampaignRepository campaignRepository;
    private final PlatformMailOutboxRepository outboxRepository;
    private final MailSenderPort mailSender;
    private final TemplateVariableRenderer templateVariableRenderer;
    private final SecurityHelper securityHelper;

    @Value("${app.platform-domain:craftive.io}")
    private String platformDomain;

    @Transactional
    public List<MailTemplateTypeSummaryDto> getTemplateTypes() {
        ensureAllFixedTemplateTypes();
        return FIXED_TEMPLATE_TYPES.stream()
            .map(this::toTemplateTypeSummary)
            .toList();
    }

    @Transactional
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

    @Transactional
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

    @Transactional(readOnly = true)
    public List<MailSubscriberDto> getSubscribersByTemplateType(String templateTypeRaw) {
        String templateType = normalizeTemplateType(templateTypeRaw);
        return subscriberSubscriptionRepository
            .findByTemplateKeyIgnoreCaseOrderBySubscriberCreatedAtDesc(templateType)
            .stream()
            .map(this::toSubscriberDto)
            .toList();
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public MailSubscriberAdminDto getSubscriber(Long subscriberId) {
        PlatformNewsletterSubscriber subscriber = subscriberRepository.findById(subscriberId)
            .orElseThrow(() -> new IllegalArgumentException("mail.marketing.subscriber.not.found"));
        List<PlatformNewsletterSubscriberSubscription> relations =
            subscriberSubscriptionRepository.findBySubscriberOrderByTemplateKeyAsc(subscriber);
        return toSubscriberAdminDto(subscriber, relations);
    }

    @Transactional
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

    @Transactional
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

    @Transactional
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

    @Transactional
    public void subscribe(String email, String source, String templateTypeRaw) {
        String templateType = normalizeTemplateType(templateTypeRaw);
        PlatformNewsletterSubscriber subscriber = subscriberRepository.findByEmailIgnoreCase(email)
            .orElseGet(PlatformNewsletterSubscriber::new);

        subscriber.setEmail(email.trim().toLowerCase());
        subscriber.setStatus(MailSubscriberStatus.PENDING_CONFIRMATION);
        subscriber.setConfirmToken(UUID.randomUUID().toString());
        if (subscriber.getUnsubscribeToken() == null || subscriber.getUnsubscribeToken().isBlank()) {
            subscriber.setUnsubscribeToken(UUID.randomUUID().toString());
        }
        subscriber.setConfirmedAt(null);
        subscriber.setUnsubscribedAt(null);
        PlatformNewsletterSubscriber savedSubscriber = subscriberRepository.save(subscriber);
        upsertTemplateSubscription(savedSubscriber, templateType, source, LANGUAGE_EN);

        String confirmLink = buildPlatformUrl("/newsletter/confirm?token=" + savedSubscriber.getConfirmToken());
        String body = "Please confirm your newsletter subscription: " + confirmLink;
        mailSender.send(savedSubscriber.getEmail(), "Newsletter Confirmation", body);
    }

    @Transactional
    public void confirm(String token) {
        PlatformNewsletterSubscriber subscriber = subscriberRepository.findByConfirmToken(token)
            .orElseThrow(() -> new IllegalArgumentException("mail.marketing.newsletter.confirm.token.invalid"));
        subscriber.setStatus(MailSubscriberStatus.ACTIVE);
        subscriber.setConfirmedAt(LocalDateTime.now());
        subscriber.setConfirmToken(null);
        subscriberRepository.save(subscriber);
    }

    @Transactional
    public void unsubscribe(String token) {
        PlatformNewsletterSubscriber subscriber = subscriberRepository.findByUnsubscribeToken(token)
            .orElseThrow(() -> new IllegalArgumentException("mail.marketing.newsletter.unsubscribe.token.invalid"));
        subscriber.setStatus(MailSubscriberStatus.UNSUBSCRIBED);
        subscriber.setUnsubscribedAt(LocalDateTime.now());
        subscriberRepository.save(subscriber);
    }

    @Transactional
    public MailCampaignDto sendCampaign(Long templateId) {
        PlatformEmailTemplate selectedTemplate = templateRepository.findById(templateId)
            .orElseThrow(() -> new IllegalArgumentException("mail.marketing.template.not.found"));

        if (!Boolean.TRUE.equals(selectedTemplate.getIsActive())) {
            throw new IllegalStateException("mail.marketing.template.not.active");
        }

        List<PlatformNewsletterSubscriberSubscription> subscriptions = subscriberSubscriptionRepository
            .findByTemplateKeyIgnoreCaseAndPermissionTrueAndSubscriberStatus(
                selectedTemplate.getTemplateKey(),
                MailSubscriberStatus.ACTIVE
            );
        List<PlatformEmailTemplate> templateTranslations =
            templateRepository.findByTemplateKeyIgnoreCaseOrderByLanguageAsc(selectedTemplate.getTemplateKey());

        PlatformMailCampaign campaign = new PlatformMailCampaign();
        campaign.setTemplate(selectedTemplate);
        campaign.setSubject(selectedTemplate.getSubject());
        campaign.setContent(selectedTemplate.getContent());
        campaign.setStatus(MailCampaignStatus.SENDING);
        campaign.setTotalCount(subscriptions.size());
        campaign.setCreatedBy(resolveCurrentUserEmail());
        campaign = campaignRepository.save(campaign);

        int sent = 0;
        int failed = 0;
        for (PlatformNewsletterSubscriberSubscription subscription : subscriptions) {
            PlatformNewsletterSubscriber subscriber = subscription.getSubscriber();
            PlatformEmailTemplate resolvedTemplate = resolveTemplateForLanguage(
                templateTranslations,
                normalizePreferredLanguage(subscription.getPreferredLanguage()),
                selectedTemplate
            );
            PlatformMailOutbox outbox = new PlatformMailOutbox();
            outbox.setCampaign(campaign);
            outbox.setToEmail(subscriber.getEmail());
            outbox.setSubject(render(resolvedTemplate.getSubject(), subscriber));
            outbox.setContent(render(resolvedTemplate.getContent(), subscriber));
            outbox.setStatus(MailOutboxStatus.PROCESSING);
            outbox = outboxRepository.save(outbox);

            EmailResult result = mailSender.send(outbox.getToEmail(), outbox.getSubject(), outbox.getContent());
            if (result.isSuccess()) {
                outbox.setStatus(MailOutboxStatus.SENT);
                outbox.setProviderMessageId(result.getMessageId());
                sent++;
            } else {
                outbox.setStatus(MailOutboxStatus.FAILED);
                outbox.setErrorMessage(result.getErrorMessage());
                failed++;
            }
            outboxRepository.save(outbox);
        }

        campaign.setSentCount(sent);
        campaign.setFailedCount(failed);
        campaign.setStatus(failed > 0 ? MailCampaignStatus.COMPLETED_WITH_ERRORS : MailCampaignStatus.COMPLETED);
        campaign = campaignRepository.save(campaign);

        return toCampaignDto(campaign);
    }

    @Transactional(readOnly = true)
    public MailCampaignDto getCampaign(Long campaignId) {
        PlatformMailCampaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new IllegalArgumentException("mail.marketing.campaign.not.found"));
        return toCampaignDto(campaign);
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

    private void upsertTemplateSubscription(
        PlatformNewsletterSubscriber subscriber,
        String templateType,
        String source,
        String preferredLanguage
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
        relation.setPermission(Boolean.TRUE);
        subscriberSubscriptionRepository.save(relation);
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
        try {
            return securityHelper.getCurrentUserEmail();
        } catch (Exception ex) {
            return "system";
        }
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
            campaign.getFailedCount()
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
