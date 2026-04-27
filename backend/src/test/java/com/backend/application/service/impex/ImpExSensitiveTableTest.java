package com.backend.application.service.impex;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * SEC-100: ensures DML against authn/authz/tenant tables is rejected by ImpEx
 * before reaching JDBC.
 */
class ImpExSensitiveTableTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "INSERT INTO users (id, role) VALUES (1, 'TENANT_ADMIN') | users",
            "UPDATE users SET role='SUPER_ADMIN' WHERE id=1 | users",
            "INSERT INTO platform_admin_users (id) VALUES (1) | platform_admin_users",
            "UPDATE tenants SET status='ACTIVE' WHERE id=1 | tenants",
            "UPDATE tenant_modules SET enabled=1 WHERE id=1 | tenant_modules",
            "INSERT INTO config_property (k, v) VALUES ('a', 'b') | config_property",
            "UPDATE platform_settings SET value='x' WHERE name='y' | platform_settings",
            "UPDATE platform_refresh_tokens SET revoked_at=NULL WHERE id=1 | platform_refresh_tokens"
    })
    void sensitiveTableTarget_blocksWriteToSensitiveTables(String sql, String expected) {
        assertThat(ImpExServiceImpl.sensitiveTableTarget(sql)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "INSERT INTO sites (id, name) VALUES (1, 'Demo')",
            "UPDATE products SET price=10 WHERE sku='X'",
            "INSERT INTO components (uid, type) VALUES ('hero-1', 'HeroBanner')",
            "UPDATE pages SET title='Home' WHERE id=1"
    })
    void sensitiveTableTarget_allowsRegularContentTables(String sql) {
        assertThat(ImpExServiceImpl.sensitiveTableTarget(sql)).isNull();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "INSERT INTO `users` (id) VALUES (1) | users",
            "INSERT INTO platform_management.platform_admin_users (id) VALUES (1) | platform_admin_users",
            "update USERS set role='X' | users",
            "  insert  into  Tenants  (id)  values  (1)   | tenants"
    })
    void sensitiveTableTarget_handlesQuotingSchemaPrefixAndCase(String sql, String expected) {
        assertThat(ImpExServiceImpl.sensitiveTableTarget(sql)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * FROM users",
            "SELECT id, role FROM platform_admin_users",
            "  SELECT 1  "
    })
    void sensitiveTableTarget_doesNotInterfereWithSelect(String sql) {
        assertThat(ImpExServiceImpl.sensitiveTableTarget(sql)).isNull();
    }
}
