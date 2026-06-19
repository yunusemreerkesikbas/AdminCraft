package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.backend.application.commerce.dto.CommerceOrderDetailResponse;
import com.backend.application.commerce.dto.CommerceOrderSummaryResponse;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommerceOrder;
import com.backend.domain.commerce.CommerceOrderItem;
import com.backend.domain.commerce.CommerceOrderLegalSnapshotStatus;
import com.backend.domain.commerce.CommerceOrderStatus;
import com.backend.domain.commerce.repository.CommerceOrderRepository;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.testutil.BaseServiceTest;
import com.fasterxml.jackson.databind.ObjectMapper;

class CommerceOrderReadServiceImplTest extends BaseServiceTest {

	@Mock private CommerceOrderRepository orderRepository;
	@Mock private CommerceModuleAccessGuard commerceModuleAccessGuard;

	private CommerceOrderReadServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new CommerceOrderReadServiceImpl(orderRepository, commerceModuleAccessGuard, new ObjectMapper());
	}

	@Test
	void list_ShouldReturnAuthenticatedCustomersOrdersWithItemCounts() {
		CommerceCustomerPrincipal principal = principal(10L);
		CommerceOrder order = order(1L, "order-uid", customer(10L));
		when(orderRepository.findByCustomerId(10L, PageRequest.of(0, 20)))
				.thenReturn(new PageImpl<>(List.of(order)));
		when(orderRepository.countItemsByOrderIds(List.of(1L))).thenReturn(Map.of(1L, 2));

		var result = service.list(principal, PageRequest.of(0, 20));

		assertThat(result.getContent()).hasSize(1);
		CommerceOrderSummaryResponse response = result.getContent().getFirst();
		assertThat(response.orderUid()).isEqualTo("order-uid");
		assertThat(response.itemCount()).isEqualTo(2);
		verify(commerceModuleAccessGuard).assertEnabledForCurrentTenant();
	}

	@Test
	void get_ShouldReturnDetailWithItemsAddressesShippingAndLegalSnapshotStatus() {
		CommerceCustomerPrincipal principal = principal(10L);
		CommerceOrder order = order(1L, "order-uid", customer(10L));
		order.addItem(orderItem());
		when(orderRepository.findByCustomerIdAndUid(10L, "order-uid")).thenReturn(Optional.of(order));

		CommerceOrderDetailResponse response = service.get(principal, "order-uid");

		assertThat(response.orderUid()).isEqualTo("order-uid");
		assertThat(response.items()).hasSize(1);
		assertThat(response.items().getFirst().variantUid()).isEqualTo("variant-uid");
		assertThat(response.totals().total()).isEqualByComparingTo("200.00");
		assertThat(response.shipping().methodCode()).isEqualTo("STANDARD");
		assertThat(response.deliveryAddress().city()).isEqualTo("Istanbul");
		assertThat(response.billingAddress().district()).isEqualTo("Kadikoy");
		assertThat(response.legalSnapshotStatus()).isEqualTo("NOT_CAPTURED");
	}

	@Test
	void get_ShouldTreatOtherCustomerOrderAsNotFound() {
		CommerceCustomerPrincipal principal = principal(10L);
		when(orderRepository.findByCustomerIdAndUid(10L, "other-order-uid")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.get(principal, "other-order-uid"))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("commerce.order.not.found");
	}

	@Test
	void customerResponses_ShouldNotExposeRawProviderFields() {
		List<String> summaryFields = Arrays.stream(CommerceOrderSummaryResponse.class.getRecordComponents())
				.map(RecordComponent::getName)
				.toList();
		List<String> detailFields = Arrays.stream(CommerceOrderDetailResponse.class.getRecordComponents())
				.map(RecordComponent::getName)
				.toList();

		assertThat(summaryFields).doesNotContain(
				"provider",
				"providerTransactionId",
				"requiresAttention",
				"attentionReasonKey");
		assertThat(detailFields).doesNotContain(
				"provider",
				"providerTransactionId",
				"requiresAttention",
				"attentionReasonKey");
	}

	private CommerceCustomerPrincipal principal(Long customerId) {
		return new CommerceCustomerPrincipal(customerId, "customer-uid", "customer@example.com", 1L);
	}

	private CommerceOrder order(Long id, String uid, CommerceCustomer customer) {
		CommerceOrder order = new CommerceOrder();
		order.setId(id);
		order.setUid(uid);
		order.setOrderNumber("ORD-20260615-000001");
		order.setCreatedAt(LocalDateTime.of(2026, 6, 15, 12, 0));
		order.setCustomer(customer);
		order.setStatus(CommerceOrderStatus.PAID);
		order.setCurrencyIso("TRY");
		order.setSubtotal(BigDecimal.valueOf(166.67));
		order.setVatTotal(BigDecimal.valueOf(33.33));
		order.setShippingTotal(BigDecimal.ZERO.setScale(2));
		order.setTotal(BigDecimal.valueOf(200).setScale(2));
		order.setShippingMethodCode("STANDARD");
		order.setShippingMethodName("commerce.shipping.method.standard");
		order.setDeliveryAddressUid("delivery-address-uid");
		order.setBillingAddressUid("billing-address-uid");
		order.setDeliveryAddressSnapshot(addressSnapshot());
		order.setBillingAddressSnapshot(addressSnapshot());
		order.setProvider("iyzico");
		order.setProviderTransactionId("payment-123");
		order.setLegalSnapshotStatus(CommerceOrderLegalSnapshotStatus.NOT_CAPTURED);
		order.setRequiresAttention(true);
		order.setAttentionReasonKey("commerce.order.attention.stock_not_deducted");
		return order;
	}

	private CommerceOrderItem orderItem() {
		CommerceOrderItem item = new CommerceOrderItem();
		item.setUid("order-item-uid");
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

	private CommerceCustomer customer(Long id) {
		CommerceCustomer customer = new CommerceCustomer();
		customer.setId(id);
		customer.setUid("customer-uid");
		customer.setEmail("customer@example.com");
		customer.setFirstName("Jane");
		customer.setLastName("Doe");
		customer.setPhone("+905350000000");
		return customer;
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
