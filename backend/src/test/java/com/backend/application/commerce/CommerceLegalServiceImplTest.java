package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.backend.application.commerce.dto.CommerceLegalAcceptanceCommand;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.application.service.mail.TemplateVariableRenderer;
import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartStatus;
import com.backend.domain.commerce.CommerceCheckout;
import com.backend.domain.commerce.CommerceCheckoutItem;
import com.backend.domain.commerce.CommerceCheckoutStatus;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommerceLegalTemplate;
import com.backend.domain.commerce.CommerceLegalTemplateStatus;
import com.backend.domain.commerce.CommerceLegalTemplateType;
import com.backend.domain.commerce.repository.CommerceLegalTemplateRepository;
import com.backend.domain.port.TenantContextPort;
import com.backend.testutil.BaseServiceTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class CommerceLegalServiceImplTest extends BaseServiceTest {

	@Mock private CommerceLegalTemplateRepository templateRepository;
	@Mock private CommerceModuleAccessGuard commerceModuleAccessGuard;
	@Mock private ConfigPropertyService configPropertyService;
	@Mock private TenantContextPort tenantContext;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
	private CommerceLegalServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new CommerceLegalServiceImpl(
				templateRepository,
				commerceModuleAccessGuard,
				configPropertyService,
				tenantContext,
				new TemplateVariableRenderer(),
				objectMapper);
		lenient().when(tenantContext.getTenantId()).thenReturn("1");
		lenient().when(tenantContext.getTenantDbName()).thenReturn("tenant_1");
	}

	@Test
	void captureAcceptanceJson_ShouldRenderDocumentsAndCheckoutMetadata() throws Exception {
		stubSellerConfig();
		CommerceLegalTemplate distance = template(
				1L,
				"distance-tr",
				CommerceLegalTemplateType.DISTANCE_SALES_AGREEMENT,
				"TR",
				1,
				"Distance",
				"Seller {{seller.name}} customer {{customer.firstName}} total {{order.total}} city {{delivery.city}}");
		CommerceLegalTemplate preInfo = template(
				2L,
				"preinfo-tr",
				CommerceLegalTemplateType.PRE_INFORMATION_FORM,
				"TR",
				1,
				"Pre Info",
				"Items {{order.itemCount}} shipping {{order.shippingTotal}}");
		stubPublished("TR", distance, preInfo);

		String json = service.captureAcceptanceJson(
				checkout(),
				"tr-TR",
				List.of(
						new CommerceLegalAcceptanceCommand("distance-tr", 1, true),
						new CommerceLegalAcceptanceCommand("preinfo-tr", 1, true)));

		JsonNode snapshot = objectMapper.readTree(json);
		assertThat(snapshot.get("language").asText()).isEqualTo("TR");
		assertThat(snapshot.get("documents")).hasSize(2);
		assertThat(snapshot.at("/documents/0/contentText").asText())
				.contains("Seller Seller Inc", "customer John", "total 200.00", "city Istanbul");
		assertThat(snapshot.at("/documents/0/contentHash").asText()).isNotBlank();
		assertThat(snapshot.at("/checkout/checkoutUid").asText()).isEqualTo("checkout-uid");
		assertThat(snapshot.at("/checkout/total").asText()).isEqualTo("200.00");
	}

	@Test
	void captureAcceptanceJson_ShouldRejectStaleAcceptanceVersion() {
		stubSellerConfig();
		CommerceLegalTemplate distance = template(
				1L,
				"distance-tr",
				CommerceLegalTemplateType.DISTANCE_SALES_AGREEMENT,
				"TR",
				2,
				"Distance",
				"Distance content");
		CommerceLegalTemplate preInfo = template(
				2L,
				"preinfo-tr",
				CommerceLegalTemplateType.PRE_INFORMATION_FORM,
				"TR",
				1,
				"Pre Info",
				"Pre info content");
		stubPublished("TR", distance, preInfo);

		assertThatThrownBy(() -> service.captureAcceptanceJson(
				checkout(),
				"TR",
				List.of(
						new CommerceLegalAcceptanceCommand("distance-tr", 1, true),
						new CommerceLegalAcceptanceCommand("preinfo-tr", 1, true))))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("commerce.legal.acceptance.stale");
	}

	@Test
	void legalForCheckout_ShouldRequireExactLanguageTemplates() {
		stubSellerConfig();
		when(templateRepository.findByTypeAndLanguageAndStatus(
				CommerceLegalTemplateType.DISTANCE_SALES_AGREEMENT,
				"EN",
				CommerceLegalTemplateStatus.PUBLISHED))
				.thenReturn(Optional.empty());
		when(templateRepository.findByTypeAndLanguageAndStatus(
				CommerceLegalTemplateType.PRE_INFORMATION_FORM,
				"EN",
				CommerceLegalTemplateStatus.PUBLISHED))
				.thenReturn(Optional.empty());

		var response = service.legalForCheckout(checkout(), "en-US");

		assertThat(response.ready()).isFalse();
		assertThat(response.language()).isEqualTo("EN");
		assertThat(response.documents()).isEmpty();
		assertThat(response.missingReasons()).containsExactly("commerce.legal.templates.missing");
	}

	@Test
	void publishTemplate_ShouldLockTypeLanguageAndArchivePreviousPublishedTemplate() {
		CommerceLegalTemplate published = template(
				1L,
				"distance-tr-v1",
				CommerceLegalTemplateType.DISTANCE_SALES_AGREEMENT,
				"TR",
				1,
				"Distance v1",
				"Published content");
		published.setStatus(CommerceLegalTemplateStatus.PUBLISHED);
		CommerceLegalTemplate draft = template(
				2L,
				"distance-tr-v2",
				CommerceLegalTemplateType.DISTANCE_SALES_AGREEMENT,
				"TR",
				2,
				"Distance v2",
				"Draft content");
		when(templateRepository.findByUid("distance-tr-v2")).thenReturn(Optional.of(draft));
		when(templateRepository.findByTypeAndLanguageForUpdate(
				CommerceLegalTemplateType.DISTANCE_SALES_AGREEMENT,
				"TR"))
				.thenReturn(List.of(published, draft));
		when(templateRepository.save(published)).thenReturn(published);
		when(templateRepository.save(draft)).thenReturn(draft);

		var response = service.publishTemplate("distance-tr-v2");

		assertThat(response.status()).isEqualTo("PUBLISHED");
		assertThat(published.getStatus()).isEqualTo(CommerceLegalTemplateStatus.ARCHIVED);
		assertThat(draft.getStatus()).isEqualTo(CommerceLegalTemplateStatus.PUBLISHED);
		assertThat(draft.getPublishedAt()).isNotNull();
		InOrder inOrder = inOrder(templateRepository);
		inOrder.verify(templateRepository).findByUid("distance-tr-v2");
		inOrder.verify(templateRepository).findByTypeAndLanguageForUpdate(
				CommerceLegalTemplateType.DISTANCE_SALES_AGREEMENT,
				"TR");
		inOrder.verify(templateRepository).save(published);
		inOrder.verify(templateRepository).save(draft);
	}

	private void stubSellerConfig() {
		when(configPropertyService.findRaw(1L, "tenant_1", "commerce.legal.seller_name"))
				.thenReturn(Optional.of("Seller Inc"));
		when(configPropertyService.findRaw(1L, "tenant_1", "commerce.legal.seller_address"))
				.thenReturn(Optional.of("Seller Street"));
		when(configPropertyService.findRaw(1L, "tenant_1", "commerce.legal.seller_email"))
				.thenReturn(Optional.of("seller@example.com"));
		when(configPropertyService.findRaw(1L, "tenant_1", "commerce.legal.seller_phone"))
				.thenReturn(Optional.of("+905550000000"));
	}

	private void stubPublished(
			String language,
			CommerceLegalTemplate distance,
			CommerceLegalTemplate preInfo) {
		when(templateRepository.findByTypeAndLanguageAndStatus(
				CommerceLegalTemplateType.DISTANCE_SALES_AGREEMENT,
				language,
				CommerceLegalTemplateStatus.PUBLISHED))
				.thenReturn(Optional.of(distance));
		when(templateRepository.findByTypeAndLanguageAndStatus(
				CommerceLegalTemplateType.PRE_INFORMATION_FORM,
				language,
				CommerceLegalTemplateStatus.PUBLISHED))
				.thenReturn(Optional.of(preInfo));
	}

	private CommerceLegalTemplate template(
			Long id,
			String uid,
			CommerceLegalTemplateType type,
			String language,
			int version,
			String title,
			String contentText) {
		CommerceLegalTemplate template = new CommerceLegalTemplate();
		template.setId(id);
		template.setUid(uid);
		template.setType(type);
		template.setLanguage(language);
		template.setVersion(version);
		template.setStatus(CommerceLegalTemplateStatus.DRAFT);
		template.setTitle(title);
		template.setContentText(contentText);
		return template;
	}

	private CommerceCheckout checkout() {
		CommerceCustomer customer = new CommerceCustomer();
		customer.setId(10L);
		customer.setUid("customer-uid");
		customer.setEmail("customer@example.com");
		customer.setFirstName("John");
		customer.setLastName("Doe");
		customer.setPhone("+905350000000");
		CommerceCart cart = new CommerceCart();
		cart.setId(20L);
		cart.setUid("cart-uid");
		cart.setCustomer(customer);
		cart.setStatus(CommerceCartStatus.ACTIVE);
		cart.setExpiresAt(LocalDateTime.now().plusDays(1));
		CommerceCheckout checkout = new CommerceCheckout();
		checkout.setId(30L);
		checkout.setUid("checkout-uid");
		checkout.setCustomer(customer);
		checkout.setCart(cart);
		checkout.setStatus(CommerceCheckoutStatus.READY);
		checkout.setCurrencyIso("TRY");
		checkout.setSubtotal(BigDecimal.valueOf(200).setScale(2));
		checkout.setVatTotal(BigDecimal.valueOf(33.33));
		checkout.setShippingTotal(BigDecimal.ZERO.setScale(2));
		checkout.setTotal(BigDecimal.valueOf(200).setScale(2));
		checkout.setShippingMethodCode("standard");
		checkout.setShippingMethodName("Standard Shipping");
		checkout.setDeliveryAddressUid("delivery-address-uid");
		checkout.setBillingAddressUid("billing-address-uid");
		checkout.setDeliveryAddressSnapshot(addressSnapshot());
		checkout.setBillingAddressSnapshot(addressSnapshot());
		checkout.setExpiresAt(LocalDateTime.now().plusHours(1));
		checkout.addItem(checkoutItem());
		return checkout;
	}

	private CommerceCheckoutItem checkoutItem() {
		CommerceCheckoutItem item = new CommerceCheckoutItem();
		item.setUid("checkout-item-uid");
		item.setProductUid("product-uid");
		item.setProductSku("PROD-1");
		item.setVariantUid("variant-uid");
		item.setVariantSku("VAR-1");
		item.setQuantity(2);
		item.setUnitGrossPrice(BigDecimal.valueOf(100).setScale(2));
		item.setVatRate(BigDecimal.valueOf(20).setScale(2));
		item.setLineTotal(BigDecimal.valueOf(200).setScale(2));
		item.setLineVatTotal(BigDecimal.valueOf(33.33));
		return item;
	}

	private String addressSnapshot() {
		return """
				{
				  "uid": "address-uid",
				  "label": "Home",
				  "firstName": "Jane",
				  "lastName": "Doe",
				  "phone": "+905350000000",
				  "countryIso": "TR",
				  "city": "Istanbul",
				  "district": "Kadikoy",
				  "addressLine1": "Test Street 1",
				  "addressLine2": null,
				  "postalCode": "34710",
				  "invoiceType": "INDIVIDUAL",
				  "companyName": null,
				  "taxNumber": null,
				  "taxOffice": null,
				  "invoiceIdentityNumber": null
				}
				""";
	}
}
