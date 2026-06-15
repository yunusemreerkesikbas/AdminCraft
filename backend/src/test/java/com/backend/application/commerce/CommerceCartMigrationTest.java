package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class CommerceCartMigrationTest {

    @Test
    void cartFoundationMigration_ShouldUseTenantDatabaseAndTokenHash() throws Exception {
		try (var resource = getClass().getClassLoader()
				.getResourceAsStream("db/tenant/commerce/V1.0.1__cart_foundation.sql")) {
			assertThat(resource).isNotNull();
			String migration = new String(
					resource.readAllBytes(),
					StandardCharsets.UTF_8)
					.toLowerCase();

			assertThat(migration).contains("commerce_carts");
			assertThat(migration).contains("token_hash");
			assertThat(migration).contains("uk_commerce_cart_item_cart_variant");
			assertThat(migration).doesNotContain("tenant_id");
			assertThat(Pattern.compile("\\bcart_token\\b").matcher(migration).find()).isFalse();
		}
    }

	@Test
	void customerCartBridgeMigration_ShouldUseNullableCustomerReferenceWithoutTenantId() throws Exception {
		try (var resource = getClass().getClassLoader()
				.getResourceAsStream("db/tenant/commerce/V1.0.3__customer_cart_bridge.sql")) {
			assertThat(resource).isNotNull();
			String migration = new String(
					resource.readAllBytes(),
					StandardCharsets.UTF_8)
					.toLowerCase();

			assertThat(migration).contains("add column customer_id bigint null");
			assertThat(migration).contains("fk_commerce_cart_customer");
			assertThat(migration).contains("references commerce_customers(id)");
			assertThat(migration).contains("idx_commerce_cart_customer_status_expires");
			assertThat(migration).doesNotContain("tenant_id");
		}
	}

	@Test
	void checkoutMigration_ShouldCreateCheckoutSnapshotTablesWithoutTenantId() throws Exception {
		try (var resource = getClass().getClassLoader()
				.getResourceAsStream("db/tenant/commerce/V1.0.4__checkout_foundation.sql")) {
			assertThat(resource).isNotNull();
			String migration = new String(
					resource.readAllBytes(),
					StandardCharsets.UTF_8)
					.toLowerCase();

			assertThat(migration).contains("create table commerce_checkouts");
			assertThat(migration).contains("create table commerce_checkout_items");
			assertThat(migration).contains("delivery_address_snapshot json not null");
			assertThat(migration).contains("billing_address_snapshot json not null");
			assertThat(migration).contains("fk_commerce_checkout_customer");
			assertThat(migration).contains("fk_commerce_checkout_cart");
			assertThat(migration).contains("uk_commerce_checkout_item_checkout_variant");
			assertThat(migration).doesNotContain("tenant_id");
		}
	}

	@Test
	void paymentAttemptMigration_ShouldCreatePaymentAttemptSnapshotTableWithoutTenantId() throws Exception {
		try (var resource = getClass().getClassLoader()
				.getResourceAsStream("db/tenant/commerce/V1.0.5__payment_attempt_foundation.sql")) {
			assertThat(resource).isNotNull();
			String migration = new String(
					resource.readAllBytes(),
					StandardCharsets.UTF_8)
					.toLowerCase();

			assertThat(migration).contains("create table commerce_payment_attempts");
			assertThat(migration).contains("fk_commerce_payment_attempt_customer");
			assertThat(migration).contains("fk_commerce_payment_attempt_checkout");
			assertThat(migration).contains("idx_commerce_payment_attempt_customer_status_expires");
			assertThat(migration).contains("idx_commerce_payment_attempt_checkout_status");
			assertThat(migration).contains("provider_reference");
			assertThat(migration).contains("provider_transaction_id");
			assertThat(migration).contains("subtotal decimal(15,2)");
			assertThat(migration).contains("vat_total decimal(15,2)");
			assertThat(migration).contains("shipping_total decimal(15,2)");
			assertThat(migration).contains("total decimal(15,2)");
			assertThat(migration).doesNotContain("tenant_id");
		}
	}

	@Test
	void orderMigration_ShouldCreateOrderSnapshotTablesAndDailyCounterWithoutTenantId() throws Exception {
		try (var resource = getClass().getClassLoader()
				.getResourceAsStream("db/tenant/commerce/V1.0.6__order_foundation.sql")) {
			assertThat(resource).isNotNull();
			String migration = new String(
					resource.readAllBytes(),
					StandardCharsets.UTF_8)
					.toLowerCase();

			assertThat(migration).contains("create table commerce_orders");
			assertThat(migration).contains("create table commerce_order_items");
			assertThat(migration).contains("create table commerce_order_number_counters");
			assertThat(migration).contains("uk_commerce_order_checkout");
			assertThat(migration).contains("uk_commerce_order_payment_attempt");
			assertThat(migration).contains("uk_commerce_order_number");
			assertThat(migration).contains("uk_commerce_order_number_counter_prefix_date");
			assertThat(migration).contains("legal_snapshot_status");
			assertThat(migration).contains("legal_snapshot_json json null");
			assertThat(migration).contains("requires_attention");
			assertThat(migration).contains("stock_deducted");
			assertThat(migration).doesNotContain("tenant_id");
		}
	}
}
