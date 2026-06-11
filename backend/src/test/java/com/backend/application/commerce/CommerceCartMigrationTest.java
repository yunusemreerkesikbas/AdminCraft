package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class CommerceCartMigrationTest {

    @Test
    void cartFoundationMigration_ShouldUseTenantDatabaseAndTokenHash() throws Exception {
        String migration = new String(
                getClass().getClassLoader()
                        .getResourceAsStream("db/tenant/commerce/V1.0.1__cart_foundation.sql")
                        .readAllBytes(),
                StandardCharsets.UTF_8)
                .toLowerCase();

        assertThat(migration).contains("commerce_carts");
        assertThat(migration).contains("token_hash");
        assertThat(migration).contains("uk_commerce_cart_item_cart_variant");
        assertThat(migration).doesNotContain("tenant_id");
        assertThat(migration).doesNotContain(" cart_token ");
    }
}
