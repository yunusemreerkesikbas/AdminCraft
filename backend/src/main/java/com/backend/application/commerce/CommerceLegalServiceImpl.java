package com.backend.application.commerce;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.commerce.dto.CheckoutAddressSnapshotResponse;
import com.backend.application.commerce.dto.CheckoutLegalResponse;
import com.backend.application.commerce.dto.CommerceLegalAcceptanceCommand;
import com.backend.application.commerce.dto.CommerceLegalDocumentResponse;
import com.backend.application.commerce.dto.CommerceLegalTemplateCommand;
import com.backend.application.commerce.dto.CommerceLegalTemplatePreviewResponse;
import com.backend.application.commerce.dto.CommerceLegalTemplateResponse;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.application.service.mail.TemplateVariableRenderer;
import com.backend.domain.commerce.CommerceCheckout;
import com.backend.domain.commerce.CommerceCheckoutItem;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommerceLegalTemplate;
import com.backend.domain.commerce.CommerceLegalTemplateStatus;
import com.backend.domain.commerce.CommerceLegalTemplateType;
import com.backend.domain.commerce.repository.CommerceLegalTemplateRepository;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.port.TenantContextPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class CommerceLegalServiceImpl implements CommerceLegalService {

	private static final String TEMPLATE_NOT_FOUND = "commerce.legal.template.not.found";
	private static final String MISSING_TEMPLATE_KEY = "commerce.legal.templates.missing";
	private static final String SELLER_CONFIG_MISSING_KEY = "commerce.legal.seller.config.missing";
	private static final String ACCEPTANCE_REQUIRED_KEY = "commerce.legal.acceptance.required";
	private static final String ACCEPTANCE_STALE_KEY = "commerce.legal.acceptance.stale";
	private static final List<CommerceLegalTemplateType> REQUIRED_TYPES = List.of(
			CommerceLegalTemplateType.DISTANCE_SALES_AGREEMENT,
			CommerceLegalTemplateType.PRE_INFORMATION_FORM);
	private static final List<String> REQUIRED_SELLER_KEYS = List.of(
			"commerce.legal.seller_name",
			"commerce.legal.seller_address",
			"commerce.legal.seller_email",
			"commerce.legal.seller_phone");

	private final CommerceLegalTemplateRepository templateRepository;
	private final CommerceModuleAccessGuard commerceModuleAccessGuard;
	private final ConfigPropertyService configPropertyService;
	private final TenantContextPort tenantContext;
	private final TemplateVariableRenderer templateVariableRenderer;
	private final ObjectMapper objectMapper;

	@Override
	@Transactional(readOnly = true)
	public List<CommerceLegalTemplateResponse> listTemplates(
			CommerceLegalTemplateType type,
			String language,
			CommerceLegalTemplateStatus status) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		return templateRepository.findAll(type, normalizeOptionalLanguage(language), status).stream()
				.map(CommerceLegalTemplateResponse::from)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public CommerceLegalTemplateResponse getTemplate(String templateUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		return templateRepository.findByUid(requiredUid(templateUid))
				.map(CommerceLegalTemplateResponse::from)
				.orElseThrow(() -> new EntityNotFoundException(TEMPLATE_NOT_FOUND));
	}

	@Override
	@Transactional
	public CommerceLegalTemplateResponse createTemplate(CommerceLegalTemplateCommand command) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		ValidatedTemplateInput input = validateCommand(command);
		CommerceLegalTemplate template = new CommerceLegalTemplate();
		template.setType(input.type());
		template.setLanguage(input.language());
		template.setVersion(templateRepository.nextVersion(input.type(), input.language()));
		template.setStatus(CommerceLegalTemplateStatus.DRAFT);
		template.setTitle(input.title());
		template.setContentText(input.contentText());
		return CommerceLegalTemplateResponse.from(templateRepository.save(template));
	}

	@Override
	@Transactional
	public CommerceLegalTemplateResponse updateTemplate(String templateUid, CommerceLegalTemplateCommand command) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceLegalTemplate template = mutableTemplate(templateUid);
		ValidatedTemplateInput input = validateCommand(command);
		if (template.getType() != input.type() || !template.getLanguage().equals(input.language())) {
			throw new IllegalStateException("commerce.legal.template.identity.immutable");
		}
		template.setTitle(input.title());
		template.setContentText(input.contentText());
		return CommerceLegalTemplateResponse.from(templateRepository.save(template));
	}

	@Override
	@Transactional
	public CommerceLegalTemplateResponse publishTemplate(String templateUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceLegalTemplate template = mutableTemplate(templateUid);
		if (template.getTitle() == null || template.getTitle().isBlank()
				|| template.getContentText() == null || template.getContentText().isBlank()) {
			throw new IllegalStateException("commerce.legal.template.content.required");
		}
		templateRepository.findByTypeAndLanguageForUpdate(template.getType(), template.getLanguage())
				.stream()
				.filter(existing -> existing.getStatus() == CommerceLegalTemplateStatus.PUBLISHED)
				.forEach(existing -> {
					if (!Objects.equals(existing.getId(), template.getId())) {
						existing.setStatus(CommerceLegalTemplateStatus.ARCHIVED);
						templateRepository.save(existing);
					}
				});
		template.setStatus(CommerceLegalTemplateStatus.PUBLISHED);
		template.setPublishedAt(LocalDateTime.now());
		return CommerceLegalTemplateResponse.from(templateRepository.save(template));
	}

	@Override
	@Transactional
	public CommerceLegalTemplateResponse archiveTemplate(String templateUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceLegalTemplate template = templateRepository.findByUid(requiredUid(templateUid))
				.orElseThrow(() -> new EntityNotFoundException(TEMPLATE_NOT_FOUND));
		template.setStatus(CommerceLegalTemplateStatus.ARCHIVED);
		return CommerceLegalTemplateResponse.from(templateRepository.save(template));
	}

	@Override
	@Transactional(readOnly = true)
	public CommerceLegalTemplatePreviewResponse previewTemplate(String templateUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceLegalTemplate template = templateRepository.findByUid(requiredUid(templateUid))
				.orElseThrow(() -> new EntityNotFoundException(TEMPLATE_NOT_FOUND));
		Map<String, String> sampleVariables = Map.ofEntries(
				Map.entry("seller.name", "Demo Seller"),
				Map.entry("seller.address", "Demo Address"),
				Map.entry("seller.email", "seller@example.com"),
				Map.entry("seller.phone", "+90 555 000 0000"),
				Map.entry("customer.firstName", "Demo"),
				Map.entry("customer.lastName", "Customer"),
				Map.entry("customer.email", "customer@example.com"),
				Map.entry("customer.phone", "+90 555 111 1111"),
				Map.entry("order.currencyIso", "TRY"),
				Map.entry("order.subtotal", "1000.00"),
				Map.entry("order.vatTotal", "166.67"),
				Map.entry("order.shippingTotal", "50.00"),
				Map.entry("order.total", "1050.00"),
				Map.entry("order.itemCount", "1"),
				Map.entry("order.items", "DEMO-SKU x 1 = 1000.00"),
				Map.entry("delivery.city", "Istanbul"),
				Map.entry("delivery.district", "Kadikoy"),
				Map.entry("billing.city", "Istanbul"),
				Map.entry("billing.district", "Kadikoy"));
		return new CommerceLegalTemplatePreviewResponse(
				template.getUid(),
				templateVariableRenderer.render(template.getContentText(), sampleVariables));
	}

	@Override
	@Transactional(readOnly = true)
	public CheckoutLegalResponse legalForCheckout(CommerceCheckout checkout, String language) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		return buildLegal(checkout, normalizeLanguage(language));
	}

	@Override
	@Transactional(readOnly = true)
	public String captureAcceptanceJson(
			CommerceCheckout checkout,
			String language,
			List<CommerceLegalAcceptanceCommand> acceptances) {
		CheckoutLegalResponse legal = buildLegal(checkout, normalizeLanguage(language));
		if (!legal.ready()) {
			throw new IllegalStateException(legal.missingReasons().isEmpty()
					? MISSING_TEMPLATE_KEY
					: legal.missingReasons().get(0));
		}
		Map<String, CommerceLegalAcceptanceCommand> acceptedByUid = Optional.ofNullable(acceptances)
				.orElse(List.of())
				.stream()
				.filter(acceptance -> acceptance.templateUid() != null)
				.collect(Collectors.toMap(
						acceptance -> acceptance.templateUid().trim(),
						acceptance -> acceptance,
						(left, right) -> right));
		for (CommerceLegalDocumentResponse document : legal.documents()) {
			CommerceLegalAcceptanceCommand acceptance = acceptedByUid.get(document.templateUid());
			if (acceptance == null || !Boolean.TRUE.equals(acceptance.accepted())) {
				throw new IllegalStateException(ACCEPTANCE_REQUIRED_KEY);
			}
			if (!document.version().equals(acceptance.version())) {
				throw new IllegalStateException(ACCEPTANCE_STALE_KEY);
			}
		}
		LocalDateTime capturedAt = LocalDateTime.now();
		LegalAcceptanceSnapshot snapshot = new LegalAcceptanceSnapshot(
				legal.language(),
				capturedAt,
				capturedAt,
				legal.documents(),
				new LegalCheckoutSnapshot(
						checkout.getUid(),
						checkout.getCurrencyIso(),
						moneyString(checkout.getSubtotal()),
						moneyString(checkout.getVatTotal()),
						moneyString(checkout.getShippingTotal()),
						moneyString(checkout.getTotal()),
						checkout.getItems().size()));
		try {
			return objectMapper.writeValueAsString(snapshot);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("commerce.legal.snapshot.invalid", ex);
		}
	}

	private CheckoutLegalResponse buildLegal(CommerceCheckout checkout, String language) {
		List<String> missing = new ArrayList<>();
		Map<String, String> seller = sellerVariables();
		if (seller.isEmpty()) {
			missing.add(SELLER_CONFIG_MISSING_KEY);
		}
		List<CommerceLegalDocumentResponse> documents = new ArrayList<>();
		for (CommerceLegalTemplateType type : REQUIRED_TYPES) {
			Optional<CommerceLegalTemplate> template = templateRepository.findByTypeAndLanguageAndStatus(
					type,
					language,
					CommerceLegalTemplateStatus.PUBLISHED);
			if (template.isEmpty()) {
				missing.add(MISSING_TEMPLATE_KEY);
				continue;
			}
			if (!seller.isEmpty()) {
				documents.add(renderDocument(template.get(), checkout, seller));
			}
		}
		if (!missing.isEmpty()) {
			return new CheckoutLegalResponse(false, language, missing.stream().distinct().toList(), List.of());
		}
		return new CheckoutLegalResponse(true, language, List.of(), documents);
	}

	private CommerceLegalDocumentResponse renderDocument(
			CommerceLegalTemplate template,
			CommerceCheckout checkout,
			Map<String, String> seller) {
		Map<String, String> variables = renderVariables(checkout, seller);
		String content = templateVariableRenderer.render(template.getContentText(), variables);
		return new CommerceLegalDocumentResponse(
				template.getUid(),
				template.getType(),
				template.getLanguage(),
				template.getVersion(),
				template.getTitle(),
				content,
				sha256(content));
	}

	private Map<String, String> renderVariables(CommerceCheckout checkout, Map<String, String> seller) {
		Map<String, String> variables = new LinkedHashMap<>(seller);
		CommerceCustomer customer = checkout.getCustomer();
		if (customer != null) {
			variables.put("customer.firstName", nonBlank(customer.getFirstName()));
			variables.put("customer.lastName", nonBlank(customer.getLastName()));
			variables.put("customer.email", nonBlank(customer.getEmail()));
			variables.put("customer.phone", nonBlank(customer.getPhone()));
		}
		variables.put("order.currencyIso", nonBlank(checkout.getCurrencyIso()));
		variables.put("order.subtotal", moneyString(checkout.getSubtotal()));
		variables.put("order.vatTotal", moneyString(checkout.getVatTotal()));
		variables.put("order.shippingTotal", moneyString(checkout.getShippingTotal()));
		variables.put("order.total", moneyString(checkout.getTotal()));
		variables.put("order.itemCount", String.valueOf(checkout.getItems().size()));
		variables.put("order.items", checkout.getItems().stream()
				.map(this::itemLine)
				.collect(Collectors.joining("\n")));
		addressVariables(variables, "delivery", checkout.getDeliveryAddressSnapshot());
		addressVariables(variables, "billing", checkout.getBillingAddressSnapshot());
		return variables;
	}

	private void addressVariables(Map<String, String> variables, String prefix, String json) {
		try {
			CheckoutAddressSnapshotResponse address = objectMapper.readValue(json, CheckoutAddressSnapshotResponse.class);
			variables.put(prefix + ".firstName", nonBlank(address.firstName()));
			variables.put(prefix + ".lastName", nonBlank(address.lastName()));
			variables.put(prefix + ".phone", nonBlank(address.phone()));
			variables.put(prefix + ".city", nonBlank(address.city()));
			variables.put(prefix + ".district", nonBlank(address.district()));
			variables.put(prefix + ".addressLine1", nonBlank(address.addressLine1()));
			variables.put(prefix + ".addressLine2", nonBlank(address.addressLine2()));
			variables.put(prefix + ".postalCode", nonBlank(address.postalCode()));
			variables.put(prefix + ".countryIso", nonBlank(address.countryIso()));
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("commerce.checkout.address.snapshot.invalid", ex);
		}
	}

	private Map<String, String> sellerVariables() {
		Map<String, String> values = new LinkedHashMap<>();
		for (String key : REQUIRED_SELLER_KEYS) {
			Optional<String> value = configPropertyService.findRaw(currentTenantId(), tenantContext.getTenantDbName(), key)
					.map(String::trim)
					.filter(raw -> !raw.isBlank());
			if (value.isEmpty()) {
				return Map.of();
			}
			values.put(sellerVariableKey(key), value.get());
		}
		return values;
	}

	private String sellerVariableKey(String configKey) {
		return switch (configKey) {
			case "commerce.legal.seller_name" -> "seller.name";
			case "commerce.legal.seller_address" -> "seller.address";
			case "commerce.legal.seller_email" -> "seller.email";
			case "commerce.legal.seller_phone" -> "seller.phone";
			default -> configKey;
		};
	}

	private CommerceLegalTemplate mutableTemplate(String templateUid) {
		CommerceLegalTemplate template = templateRepository.findByUid(requiredUid(templateUid))
				.orElseThrow(() -> new EntityNotFoundException(TEMPLATE_NOT_FOUND));
		if (template.getStatus() == CommerceLegalTemplateStatus.PUBLISHED) {
			throw new IllegalStateException("commerce.legal.template.published.immutable");
		}
		return template;
	}

	private ValidatedTemplateInput validateCommand(CommerceLegalTemplateCommand command) {
		if (command == null || command.type() == null) {
			throw new IllegalArgumentException("commerce.legal.template.type.required");
		}
		String language = normalizeLanguage(command.language());
		String title = requiredText(command.title(), "commerce.legal.template.title.required", 191);
		String content = requiredText(command.contentText(), "commerce.legal.template.content.required", 20000);
		return new ValidatedTemplateInput(command.type(), language, title, content);
	}

	private String normalizeOptionalLanguage(String language) {
		if (language == null || language.isBlank()) {
			return null;
		}
		return normalizeLanguage(language);
	}

	private String normalizeLanguage(String language) {
		if (language == null || language.isBlank()) {
			throw new IllegalArgumentException("commerce.legal.template.language.required");
		}
		return language.trim().split("[-_]")[0].toUpperCase(Locale.ROOT);
	}

	private String requiredUid(String uid) {
		if (uid == null || uid.isBlank()) {
			throw new IllegalArgumentException("commerce.legal.template.uid.required");
		}
		return uid.trim();
	}

	private String requiredText(String value, String messageKey, int maxLength) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(messageKey);
		}
		String trimmed = value.trim();
		if (trimmed.length() > maxLength) {
			throw new IllegalArgumentException(messageKey);
		}
		return trimmed;
	}

	private String itemLine(CommerceCheckoutItem item) {
		return item.getVariantSku() + " x " + item.getQuantity() + " = " + moneyString(item.getLineTotal());
	}

	private String moneyString(BigDecimal value) {
		return Objects.requireNonNullElse(value, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP).toPlainString();
	}

	private String nonBlank(String value) {
		return value == null ? "" : value.trim();
	}

	private String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("commerce.legal.hash.failed", ex);
		}
	}

	private Long currentTenantId() {
		try {
			return Long.parseLong(tenantContext.getTenantId());
		} catch (NumberFormatException ex) {
			throw new IllegalStateException("commerce.tenant.context.required", ex);
		}
	}

	private record ValidatedTemplateInput(
			CommerceLegalTemplateType type,
			String language,
			String title,
			String contentText) {
	}

	private record LegalAcceptanceSnapshot(
			String language,
			LocalDateTime capturedAt,
			LocalDateTime acceptedAt,
			List<CommerceLegalDocumentResponse> documents,
			LegalCheckoutSnapshot checkout) {
	}

	private record LegalCheckoutSnapshot(
			String checkoutUid,
			String currencyIso,
			String subtotal,
			String vatTotal,
			String shippingTotal,
			String total,
			int itemCount) {
	}
}
