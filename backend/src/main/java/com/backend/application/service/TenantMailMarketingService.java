package com.backend.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
import com.backend.application.dto.mail.MailProviderConfigDto;
import com.backend.application.dto.mail.MailSubscriberAdminDto;
import com.backend.application.dto.mail.MailSubscriberDto;
import com.backend.application.dto.mail.MailSubscriberSubscriptionDto;
import com.backend.application.dto.mail.MailTemplateDto;
import com.backend.application.dto.mail.MailTemplateTypeDetailDto;
import com.backend.application.dto.mail.MailTemplateTypeSummaryDto;
import com.backend.application.service.mail.TemplateVariableRenderer;
import com.backend.domain.entity.MailCampaign;
import com.backend.domain.entity.MailOutbox;
import com.backend.domain.entity.MailProviderConfig;
import com.backend.domain.entity.MailTemplate;
import com.backend.domain.entity.NewsletterSubscriber;
import com.backend.domain.entity.NewsletterSubscriberSubscription;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.MailCampaignStatus;
import com.backend.domain.enums.MailOutboxStatus;
import com.backend.domain.enums.MailSubscriberStatus;
import com.backend.domain.enums.ModuleCode;
import com.backend.domain.port.EncryptionServicePort;
import com.backend.domain.port.FrontendConfigPort;
import com.backend.domain.port.MailConfigPort;
import com.backend.domain.port.MailSenderPort;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.port.TenantMailSenderPort;
import com.backend.domain.repository.MailCampaignRepository;
import com.backend.domain.repository.MailOutboxRepository;
import com.backend.domain.repository.MailProviderConfigRepository;
import com.backend.domain.repository.MailTemplateRepository;
import com.backend.domain.repository.NewsletterSubscriberRepository;
import com.backend.domain.repository.NewsletterSubscriberSubscriptionRepository;
import com.backend.shared.common.SecurityHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantMailMarketingService {

	private static final String LANGUAGE_TR = "TR";
	private static final String LANGUAGE_EN = "EN";
	private static final List<String> SUPPORTED_LANGUAGES = List.of(LANGUAGE_TR, LANGUAGE_EN);
	private static final String NEWSLETTER_DEFAULT = "NEWSLETTER_DEFAULT";
	private static final String VERSION_UPGRADE = "VERSION_UPGRADE";
	private static final List<String> FIXED_TEMPLATE_TYPES = List.of(NEWSLETTER_DEFAULT, VERSION_UPGRADE);

	private final MailTemplateRepository templateRepository;
	private final NewsletterSubscriberRepository subscriberRepository;
	private final NewsletterSubscriberSubscriptionRepository subscriberSubscriptionRepository;
	private final MailProviderConfigRepository providerConfigRepository;
	private final MailCampaignRepository campaignRepository;
	private final MailOutboxRepository outboxRepository;
	private final TenantModuleAccessService tenantModuleAccessService;
	private final MailConfigPort mailConfig;
	private final MailSenderPort mailSender;
	private final TenantMailSenderPort tenantMailSender;
	private final EncryptionServicePort encryptionService;
	private final TemplateVariableRenderer templateVariableRenderer;
	private final FrontendConfigPort frontendConfig;
	private final TenantContextPort tenantContext;
	private final SecurityHelper securityHelper;

	@Qualifier("tenantTransactionManager")
	private final PlatformTransactionManager tenantTransactionManager;

	@Transactional
	public List<MailTemplateTypeSummaryDto> getTemplateTypes() {
		assertMailMarketingEnabled();
		ensureAllFixedTemplateTypes();
		return FIXED_TEMPLATE_TYPES.stream()
			.map(this::toTemplateTypeSummary)
			.toList();
	}

	@Transactional
	public MailTemplateTypeDetailDto getTemplateTypeDetail(String templateTypeRaw) {
		assertMailMarketingEnabled();
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
		assertMailMarketingEnabled();
		String templateType = normalizeTemplateType(templateTypeRaw);
		String language = normalizeLanguage(languageRaw);

		MailTemplate template = templateRepository
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
		assertMailMarketingEnabled();
		String templateType = normalizeTemplateType(templateTypeRaw);
		return subscriberSubscriptionRepository.findByTemplateKeyIgnoreCaseOrderBySubscriberCreatedAtDesc(templateType)
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
		assertMailMarketingEnabled();
		String templateType = normalizeOptionalTemplateType(templateTypeRaw);

		Page<NewsletterSubscriber> page = subscriberRepository.searchSubscribers(
			search,
			status,
			templateType,
			permission,
			pageable
		);
		List<Long> subscriberIds = page.getContent().stream()
			.map(NewsletterSubscriber::getId)
			.toList();
		Map<Long, List<NewsletterSubscriberSubscription>> relationMap = subscriberIds.isEmpty()
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
		assertMailMarketingEnabled();
		NewsletterSubscriber subscriber = subscriberRepository.findById(subscriberId)
			.orElseThrow(() -> new IllegalArgumentException("mail.marketing.subscriber.not.found"));
		List<NewsletterSubscriberSubscription> relations =
			subscriberSubscriptionRepository.findBySubscriberOrderByTemplateKeyAsc(subscriber);
		return toSubscriberAdminDto(subscriber, relations);
	}

	@Transactional
	public MailSubscriberAdminDto createSubscriber(
		String email,
		MailSubscriberStatus status,
		List<MailSubscriberSubscriptionDto> subscriptions
	) {
		assertMailMarketingEnabled();
		String normalizedEmail = normalizeEmail(email);
		if (subscriberRepository.existsByEmailIgnoreCase(normalizedEmail)) {
			throw new IllegalArgumentException("mail.marketing.subscriber.email.exists");
		}

		NewsletterSubscriber subscriber = new NewsletterSubscriber();
		subscriber.setEmail(normalizedEmail);
		subscriber.setConfirmToken(null);
		subscriber.setUnsubscribeToken(UUID.randomUUID().toString());
		applyAdminStatus(subscriber, normalizeAdminStatus(status, MailSubscriberStatus.ACTIVE));
		NewsletterSubscriber savedSubscriber = subscriberRepository.save(subscriber);

		List<NewsletterSubscriberSubscription> savedRelations = syncTemplateSubscriptions(savedSubscriber, subscriptions);
		return toSubscriberAdminDto(savedSubscriber, savedRelations);
	}

	@Transactional
	public MailSubscriberAdminDto updateSubscriber(
		Long subscriberId,
		String email,
		MailSubscriberStatus status,
		List<MailSubscriberSubscriptionDto> subscriptions
	) {
		assertMailMarketingEnabled();
		NewsletterSubscriber subscriber = subscriberRepository.findById(subscriberId)
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

		List<NewsletterSubscriberSubscription> savedRelations = syncTemplateSubscriptions(subscriber, subscriptions);
		return toSubscriberAdminDto(subscriber, savedRelations);
	}

	@Transactional
	public void deleteSubscriber(Long subscriberId) {
		assertMailMarketingEnabled();
		NewsletterSubscriber subscriber = subscriberRepository.findById(subscriberId)
			.orElseThrow(() -> new IllegalArgumentException("mail.marketing.subscriber.not.found"));
		subscriber.setStatus(MailSubscriberStatus.UNSUBSCRIBED);
		subscriber.setConfirmToken(null);
		subscriber.setUnsubscribedAt(LocalDateTime.now());
		subscriberRepository.save(subscriber);

		List<NewsletterSubscriberSubscription> relations =
			subscriberSubscriptionRepository.findBySubscriberOrderByTemplateKeyAsc(subscriber);
		if (!relations.isEmpty()) {
			relations.forEach(relation -> relation.setPermission(Boolean.FALSE));
			subscriberSubscriptionRepository.saveAll(relations);
		}
	}

	@Transactional(readOnly = true)
	public MailProviderConfigDto getProviderConfig() {
		assertMailMarketingEnabled();
		MailProviderConfig cfg = providerConfigRepository.findTopByOrderByIdAsc().orElse(null);
		if (cfg == null) {
			return new MailProviderConfigDto("POSTMARK", "", "", false, false);
		}
		return new MailProviderConfigDto(
			cfg.getProvider(),
			cfg.getFromEmail(),
			cfg.getFromName(),
			cfg.getIsActive(),
			cfg.getServerTokenEncrypted() != null && !cfg.getServerTokenEncrypted().isBlank()
		);
	}

	@Transactional
	public MailProviderConfigDto upsertProviderConfig(String fromEmail, String fromName, String serverToken, Boolean active) {
		assertMailMarketingEnabled();
		MailProviderConfig cfg = providerConfigRepository.findTopByOrderByIdAsc().orElseGet(() -> {
			MailProviderConfig m = new MailProviderConfig();
			m.setProvider("POSTMARK");
			m.setFromEmail(mailConfig.getFromAddress());
			m.setFromName(mailConfig.getFromName());
			return m;
		});
		if (fromEmail != null) {
			cfg.setFromEmail(fromEmail);
		}
		if (fromName != null) {
			cfg.setFromName(fromName);
		}
		if (serverToken != null && !serverToken.isBlank()) {
			cfg.setServerTokenEncrypted(encryptionService.encrypt(serverToken));
		}
		if (active != null) {
			cfg.setIsActive(active);
		}
		MailProviderConfig saved = providerConfigRepository.save(cfg);
		return new MailProviderConfigDto(
			saved.getProvider(),
			saved.getFromEmail(),
			saved.getFromName(),
			saved.getIsActive(),
			saved.getServerTokenEncrypted() != null && !saved.getServerTokenEncrypted().isBlank()
		);
	}


	@Transactional
	public void subscribe(String email, String source, String templateTypeRaw) {
		assertMailMarketingEnabled();
		String templateType = normalizeTemplateType(templateTypeRaw);
		String preferredLanguage = resolvePreferredLanguageForTenant();

		NewsletterSubscriber subscriber = subscriberRepository.findByEmailIgnoreCase(email)
			.orElseGet(NewsletterSubscriber::new);
		subscriber.setEmail(email.trim().toLowerCase());
		subscriber.setStatus(MailSubscriberStatus.PENDING_CONFIRMATION);
		subscriber.setConfirmToken(UUID.randomUUID().toString());
		if (subscriber.getUnsubscribeToken() == null || subscriber.getUnsubscribeToken().isBlank()) {
			subscriber.setUnsubscribeToken(UUID.randomUUID().toString());
		}
		subscriber.setConfirmedAt(null);
		subscriber.setUnsubscribedAt(null);
		NewsletterSubscriber savedSubscriber = subscriberRepository.save(subscriber);
		upsertTemplateSubscription(savedSubscriber, templateType, source, preferredLanguage);

		String confirmLink = buildFrontendBaseUrl() + "/newsletter/confirm?token=" + savedSubscriber.getConfirmToken();
		String body = "Please confirm your newsletter subscription: " + confirmLink;
		sendTenantEmail(savedSubscriber.getEmail(), "Newsletter Confirmation", body);
	}

	@Transactional
	public void confirm(String token) {
		assertMailMarketingEnabled();
		NewsletterSubscriber subscriber = subscriberRepository.findByConfirmToken(token)
			.orElseThrow(() -> new IllegalArgumentException("mail.marketing.newsletter.confirm.token.invalid"));
		subscriber.setStatus(MailSubscriberStatus.ACTIVE);
		subscriber.setConfirmedAt(LocalDateTime.now());
		subscriber.setConfirmToken(null);
		subscriberRepository.save(subscriber);
	}

	@Transactional
	public void unsubscribe(String token) {
		assertMailMarketingEnabled();
		NewsletterSubscriber subscriber = subscriberRepository.findByUnsubscribeToken(token)
			.orElseThrow(() -> new IllegalArgumentException("mail.marketing.newsletter.unsubscribe.token.invalid"));
		subscriber.setStatus(MailSubscriberStatus.UNSUBSCRIBED);
		subscriber.setUnsubscribedAt(LocalDateTime.now());
		subscriberRepository.save(subscriber);
	}

	// Not @Transactional — HTTP calls happen outside any transaction
	public MailCampaignDto sendCampaign(Long templateId) {
		assertMailMarketingEnabled();

		TransactionTemplate tx = new TransactionTemplate(tenantTransactionManager);

		// Phase 1: Validate + create Campaign and all Outbox records as PENDING (committed immediately)
		record CampaignSetup(Long campaignId, List<Long> outboxIds) {}
		CampaignSetup setup = tx.execute(status -> {
			MailTemplate selectedTemplate = templateRepository.findById(templateId)
				.orElseThrow(() -> new IllegalArgumentException("mail.marketing.template.not.found"));
			if (!Boolean.TRUE.equals(selectedTemplate.getIsActive())) {
				throw new IllegalStateException("mail.marketing.template.not.active");
			}

			List<NewsletterSubscriberSubscription> subscriptions =
				subscriberSubscriptionRepository.findByTemplateKeyIgnoreCaseAndPermissionTrueAndSubscriberStatus(
					selectedTemplate.getTemplateKey(),
					MailSubscriberStatus.ACTIVE
				);
			List<MailTemplate> templateTranslations =
				templateRepository.findByTemplateKeyIgnoreCaseOrderByLanguageAsc(selectedTemplate.getTemplateKey());

			MailCampaign campaign = new MailCampaign();
			campaign.setTemplate(selectedTemplate);
			campaign.setSubject(selectedTemplate.getSubject());
			campaign.setContent(selectedTemplate.getContent());
			campaign.setStatus(MailCampaignStatus.SENDING);
			campaign.setTotalCount(subscriptions.size());
			campaign.setCreatedByUserId(resolveCurrentUserId());
			campaign = campaignRepository.save(campaign);

			List<Long> outboxIds = new ArrayList<>();
			for (NewsletterSubscriberSubscription subscription : subscriptions) {
				NewsletterSubscriber subscriber = subscription.getSubscriber();
				MailTemplate resolvedTemplate = resolveTemplateForLanguage(
					templateTranslations,
					normalizePreferredLanguage(subscription.getPreferredLanguage()),
					selectedTemplate
				);
				MailOutbox outbox = new MailOutbox();
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

		// Phase 2: Send emails outside of any transaction — each outbox updated in its own mini-tx
		int sent = 0;
		int failed = 0;
		try {
			for (Long outboxId : setup.outboxIds()) {
				boolean success;
				try {
					success = Boolean.TRUE.equals(tx.execute(status -> {
						MailOutbox outbox = outboxRepository.findById(outboxId).orElse(null);
						if (outbox == null) return false;
						EmailResult result = sendTenantEmail(outbox.getToEmail(), outbox.getSubject(), outbox.getContent());
						if (result.isSuccess()) {
							outbox.setStatus(MailOutboxStatus.SENT);
							outbox.setProviderMessageId(result.getMessageId());
						} else {
							outbox.setStatus(MailOutboxStatus.FAILED);
							outbox.setErrorMessage(result.getErrorMessage());
						}
						outboxRepository.save(outbox);
						return result.isSuccess();
					}));
				} catch (Throwable t) {
					log.error("Unexpected error sending outbox id={}", outboxId, t);
					tx.execute(status -> {
						MailOutbox outbox = outboxRepository.findById(outboxId).orElse(null);
						if (outbox != null) {
							outbox.setStatus(MailOutboxStatus.FAILED);
							String msg = t.getMessage();
							outbox.setErrorMessage(msg != null && msg.length() > 500 ? msg.substring(0, 500) : msg);
							outboxRepository.save(outbox);
						}
						return null;
					});
					success = false;
				}
				if (success) sent++; else failed++;
			}
		} finally {
			// Phase 3: Always update campaign with final counts (committed immediately)
			int finalSent = sent;
			int finalFailed = failed;
			return tx.execute(status -> {
				MailCampaign campaign = campaignRepository.findById(setup.campaignId())
					.orElseThrow(() -> new IllegalStateException("Campaign not found after send"));
				campaign.setSentCount(finalSent);
				campaign.setFailedCount(finalFailed);
				campaign.setStatus(finalFailed > 0 ? MailCampaignStatus.COMPLETED_WITH_ERRORS : MailCampaignStatus.COMPLETED);
				return toCampaignDto(campaignRepository.save(campaign));
			});
		}
	}

	@Transactional(readOnly = true)
	public MailCampaignDto getCampaign(Long campaignId) {
		assertMailMarketingEnabled();
		MailCampaign campaign = campaignRepository.findById(campaignId)
			.orElseThrow(() -> new IllegalArgumentException("mail.marketing.campaign.not.found"));
		return toCampaignDto(campaign);
	}

	private void assertMailMarketingEnabled() {
		tenantModuleAccessService.assertEnabledForCurrentTenant(
			ModuleCode.MAIL_MARKETING,
			"mail.marketing.tenant.context.required",
			"mail.marketing.module.not.enabled"
		);
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
		NewsletterSubscriber subscriber,
		String templateType,
		String source,
		String preferredLanguage
	) {
		NewsletterSubscriberSubscription relation = subscriberSubscriptionRepository
			.findBySubscriberAndTemplateKeyIgnoreCase(subscriber, templateType)
			.orElseGet(() -> {
				NewsletterSubscriberSubscription created = new NewsletterSubscriberSubscription();
				created.setSubscriber(subscriber);
				created.setTemplateKey(templateType);
				return created;
			});

		relation.setSource(source);
		relation.setPreferredLanguage(preferredLanguage);
		relation.setPermission(Boolean.TRUE);
		subscriberSubscriptionRepository.save(relation);
	}

	private List<NewsletterSubscriberSubscription> syncTemplateSubscriptions(
		NewsletterSubscriber subscriber,
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

		List<NewsletterSubscriberSubscription> existing =
			subscriberSubscriptionRepository.findBySubscriberOrderByTemplateKeyAsc(subscriber);
		Map<String, NewsletterSubscriberSubscription> existingByType = existing.stream()
			.collect(Collectors.toMap(
				relation -> relation.getTemplateKey().toUpperCase(),
				relation -> relation,
				(left, right) -> left,
				LinkedHashMap::new
			));

		List<NewsletterSubscriberSubscription> toDelete = new ArrayList<>();
		for (NewsletterSubscriberSubscription relation : existing) {
			if (!normalized.containsKey(relation.getTemplateKey().toUpperCase())) {
				toDelete.add(relation);
			}
		}
		if (!toDelete.isEmpty()) {
			subscriberSubscriptionRepository.deleteAll(toDelete);
		}

		List<NewsletterSubscriberSubscription> toSave = new ArrayList<>();
		for (Map.Entry<String, MailSubscriberSubscriptionDto> entry : normalized.entrySet()) {
			String templateType = entry.getKey();
			MailSubscriberSubscriptionDto incoming = entry.getValue();
			NewsletterSubscriberSubscription relation = existingByType.get(templateType);
			if (relation == null) {
				relation = new NewsletterSubscriberSubscription();
				relation.setSubscriber(subscriber);
				relation.setTemplateKey(templateType);
			}
			relation.setSource(normalizeSource(incoming.source()));
			relation.setPreferredLanguage(normalizePreferredLanguage(incoming.preferredLanguage()));
			relation.setPermission(normalizePermission(incoming.permission()));
			toSave.add(relation);
		}

		List<NewsletterSubscriberSubscription> saved = subscriberSubscriptionRepository.saveAll(toSave);
		String firstSource = saved.stream()
			.map(NewsletterSubscriberSubscription::getSource)
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
		List<MailTemplate> templates = templateRepository.findByTemplateKeyIgnoreCaseOrderByLanguageAsc(templateType);
		boolean active = templates.stream().anyMatch(t -> Boolean.TRUE.equals(t.getIsActive()));
		List<String> languages = templates.stream()
			.map(MailTemplate::getLanguage)
			.map(String::toUpperCase)
			.distinct()
			.toList();
		Long subscriberCount = subscriberSubscriptionRepository.countByTemplateKeyIgnoreCase(templateType);
		LocalDateTime lastCampaignAt = campaignRepository
			.findTopByTemplateKeyOrderByCreatedAtDesc(templateType)
			.map(MailCampaign::getCreatedAt)
			.orElse(null);

		return new MailTemplateTypeSummaryDto(templateType, active, languages, subscriberCount, lastCampaignAt);
	}

	private MailTemplate buildDefaultTemplate(String templateType, String language) {
		MailTemplate template = new MailTemplate();
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

	private void applyAdminStatus(NewsletterSubscriber subscriber, MailSubscriberStatus status) {
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

	private String resolvePreferredLanguageForTenant() {
		// null default language falls back to EN intentionally
		return tenantContext.getDefaultLanguage() == Language.TR ? LANGUAGE_TR : LANGUAGE_EN;
	}

	private String normalizePreferredLanguage(String languageRaw) {
		if (languageRaw == null || languageRaw.isBlank()) {
			return LANGUAGE_EN;
		}
		return LANGUAGE_TR.equalsIgnoreCase(languageRaw) ? LANGUAGE_TR : LANGUAGE_EN;
	}

	private MailTemplate resolveTemplateForLanguage(
		List<MailTemplate> translations,
		String preferredLanguage,
		MailTemplate fallbackTemplate
	) {
		for (MailTemplate template : translations) {
			if (preferredLanguage.equalsIgnoreCase(template.getLanguage())
				&& Boolean.TRUE.equals(template.getIsActive())) {
				return template;
			}
		}
		for (MailTemplate template : translations) {
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

	private String render(String template, NewsletterSubscriber subscriber) {
		String name = subscriber.getEmail();
		int at = name.indexOf('@');
		if (at > 0) {
			name = name.substring(0, at);
		}
		String unsubscribeUrl = buildFrontendBaseUrl() + "/newsletter/unsubscribe?token=" + subscriber.getUnsubscribeToken();
		return templateVariableRenderer.render(template, Map.of(
			"name", name,
			"email", subscriber.getEmail(),
			"unsubscribeUrl", unsubscribeUrl
		));
	}

	private EmailResult sendTenantEmail(String to, String subject, String content) {
		if ("console".equalsIgnoreCase(mailConfig.getProvider())) {
			return mailSender.send(to, subject, content);
		}
		MailProviderConfig cfg = providerConfigRepository.findTopByOrderByIdAsc()
			.orElseThrow(() -> new IllegalStateException("mail.marketing.provider.not.configured"));
		if (!Boolean.TRUE.equals(cfg.getIsActive())) {
			throw new IllegalStateException("mail.marketing.provider.not.active");
		}
		if (cfg.getServerTokenEncrypted() == null || cfg.getServerTokenEncrypted().isBlank()) {
			throw new IllegalStateException("mail.marketing.provider.token.missing");
		}
		String token = encryptionService.decrypt(cfg.getServerTokenEncrypted());
		return tenantMailSender.send(token, cfg.getFromEmail(), cfg.getFromName(), to, subject, content);
	}

	private String buildFrontendBaseUrl() {
		String base = frontendConfig.getBaseUrl();
		if (base == null || base.isBlank()) {
			throw new IllegalStateException("mail.marketing.frontend.base.url.not.configured");
		}
		if (base.contains("%s")) {
			return String.format(base, tenantContext.getSubdomain());
		}
		return base;
	}

	private Long resolveCurrentUserId() {
		try {
			return securityHelper.getCurrentUserId();
		} catch (Exception ex) {
			return null;
		}
	}

	private MailTemplateDto toTemplateDto(MailTemplate template) {
		return new MailTemplateDto(
			template.getId(),
			template.getTemplateKey(),
			template.getLanguage(),
			template.getSubject(),
			template.getContent(),
			template.getIsActive()
		);
	}

	private MailCampaignDto toCampaignDto(MailCampaign campaign) {
		return new MailCampaignDto(
			campaign.getId(),
			campaign.getStatus(),
			campaign.getTotalCount(),
			campaign.getSentCount(),
			campaign.getFailedCount()
		);
	}

	private MailSubscriberAdminDto toSubscriberAdminDto(
		NewsletterSubscriber subscriber,
		List<NewsletterSubscriberSubscription> relations
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

	private MailSubscriberSubscriptionDto toSubscriptionDto(NewsletterSubscriberSubscription relation) {
		return new MailSubscriberSubscriptionDto(
			relation.getTemplateKey(),
			relation.getSource(),
			normalizePreferredLanguage(relation.getPreferredLanguage()),
			normalizePermission(relation.getPermission())
		);
	}

	private MailSubscriberDto toSubscriberDto(NewsletterSubscriberSubscription subscription) {
		NewsletterSubscriber subscriber = subscription.getSubscriber();
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
