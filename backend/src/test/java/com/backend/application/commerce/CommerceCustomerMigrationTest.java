package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class CommerceCustomerMigrationTest {

	@Test
	void customerFoundationMigration_ShouldUseTenantDatabaseAndSecureTokenStorage() throws Exception {
		var resource = getClass().getClassLoader()
				.getResourceAsStream("db/tenant/commerce/V1.0.2__customer_account_foundation.sql");
		assertThat(resource).isNotNull();
		String migration = new String(resource.readAllBytes(), StandardCharsets.UTF_8).toLowerCase();

		assertThat(migration).contains("commerce_customers");
		assertThat(migration).contains("commerce_customer_addresses");
		assertThat(migration).contains("commerce_customer_refresh_tokens");
		assertThat(migration).contains("token_hash");
		assertThat(migration).contains("uk_commerce_customer_email_normalized");
		assertThat(migration).doesNotContain("tenant_id");
		assertThat(migration).doesNotContain("refresh_token varchar");
	}
}
