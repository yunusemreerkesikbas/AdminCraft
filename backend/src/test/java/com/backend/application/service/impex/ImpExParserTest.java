package com.backend.application.service.impex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Locale;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.backend.application.dto.impex.ImpExResult;
import com.backend.domain.exception.ImpExInvalidScriptException;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.ImpExAuditRepository;
import com.backend.infrastructure.tenant.MultiTenantConnectionProvider;
import com.backend.shared.common.SecurityHelper;

/**
 * SEC-101: ensures ImpEx parser strips comments correctly, rejects MySQL
 * conditional comments, and applies word-boundary keyword detection so that
 * `INSERT /` + `* ... *` + `/ INTO sensitive_table` cannot bypass deny-lists.
 */
@ExtendWith(MockitoExtension.class)
class ImpExParserTest {

    private static final String MARKER = "-- #CRAFTIVE_IMPEX";
    private static final String TENANT_DB = "ac_demo_42";
    private static final String CLIENT_IP = "10.0.0.7";

    @Mock
    private MultiTenantConnectionProvider multiTenantConnectionProvider;

    @Mock
    private TenantContextPort tenantContext;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ImpExAuditRepository impExAuditRepository;

    @Mock
    private SecurityHelper securityHelper;

    private ImpExServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ImpExServiceImpl(
                multiTenantConnectionProvider,
                tenantContext,
                messageSource,
                impExAuditRepository,
                securityHelper);
    }

    // ---------- stripComments (pure helper) ----------

    @Test
    @DisplayName("stripComments removes line and block comments, keeps tokens")
    void stripComments_removesLineAndBlockComments() {
        String sql = "INSERT /* foo */ INTO products (id) VALUES (1); -- trailing\n"
                + "/* multi\nline */ UPDATE products SET id = 2;";
        String stripped = ImpExServiceImpl.stripComments(sql);
        assertThat(stripped).doesNotContain("/*").doesNotContain("*/").doesNotContain("--");
        assertThat(stripped).contains("INSERT").contains("INTO products").contains("UPDATE products");
    }

    @Test
    @DisplayName("stripComments preserves comment markers inside string literals")
    void stripComments_preservesStringLiteralContents() {
        String sql = "INSERT INTO products (id, name) VALUES (1, 'please do not /* DROP */ this');";
        String stripped = ImpExServiceImpl.stripComments(sql);
        assertThat(stripped).contains("'please do not /* DROP */ this'");
    }

    @Test
    @DisplayName("stripComments handles escaped '' inside string literals")
    void stripComments_handlesEscapedQuotes() {
        String sql = "INSERT INTO products (id, name) VALUES (1, 'don''t /* trick */ me');";
        String stripped = ImpExServiceImpl.stripComments(sql);
        assertThat(stripped).contains("'don''t /* trick */ me'");
    }

    // ---------- sensitive table bypass via comment between tokens ----------

    @Test
    @DisplayName("INSERT /*...*/ INTO users → sensitive table detected after stripping")
    void sensitiveTable_blockCommentBetweenTokens_detected() {
        String stripped = ImpExServiceImpl.stripComments(
                "INSERT /* sneaky */ INTO users (id, role) VALUES (1, 'SUPER_ADMIN')");
        assertThat(ImpExServiceImpl.sensitiveTableTarget(stripped)).isEqualTo("users");
    }

    @Test
    @DisplayName("INSERT /*...*/ INTO `users` (backticked) → sensitive table detected")
    void sensitiveTable_blockCommentBackticked_detected() {
        String stripped = ImpExServiceImpl.stripComments(
                "INSERT /* x */ INTO `users` (id) VALUES (1)");
        assertThat(ImpExServiceImpl.sensitiveTableTarget(stripped)).isEqualTo("users");
    }

    @Test
    @DisplayName("INSERT /*...*/ INTO platform_management.platform_admin_users → sensitive detected")
    void sensitiveTable_blockCommentSchemaQualified_detected() {
        String stripped = ImpExServiceImpl.stripComments(
                "INSERT /* x */ INTO platform_management.platform_admin_users (id) VALUES (1)");
        assertThat(ImpExServiceImpl.sensitiveTableTarget(stripped)).isEqualTo("platform_admin_users");
    }

    // ---------- conditional comment rejection ----------

    @ParameterizedTest
    @ValueSource(strings = {
            MARKER + "\n/*!50000 ALTER TABLE users ADD COLUMN x INT */;",
            MARKER + "\nINSERT INTO products (id) VALUES (1) /*! DROP TABLE x */;",
            MARKER + "\n/*! UPDATE users SET role='SUPER_ADMIN' */"
    })
    @DisplayName("Conditional comment /*! ... */ → ImpExInvalidScriptException")
    void conditionalComment_anywhere_rejected(String sqlContent) {
        when(messageSource.getMessage(eq("impex.error.conditional.comment"), any(), any()))
                .thenReturn("conditional comment forbidden");

        assertThatThrownBy(() -> service.execute(sqlContent, Locale.ENGLISH, CLIENT_IP))
                .isInstanceOf(ImpExInvalidScriptException.class)
                .hasMessageContaining("conditional");
    }

    @Test
    @DisplayName("`/*!` inside string literal is allowed (data, not directive)")
    void conditionalCommentMarker_insideStringLiteral_allowed() {
        DataSource ds = h2DataSource();
        when(tenantContext.getTenantDbName()).thenReturn(TENANT_DB);
        when(multiTenantConnectionProvider.getDataSource(TENANT_DB)).thenReturn(ds);

        ImpExResult result = service.execute(
                MARKER + "\nINSERT INTO products (id, name) VALUES (1, 'literal /*! data */');",
                Locale.ENGLISH, CLIENT_IP);

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.executedStatements()).isEqualTo(1);
    }

    // ---------- word-boundary keyword detection ----------

    @Test
    @DisplayName("`/* hide */DROP TABLE x` → blocked as DROP after stripping")
    void blockComment_prefixingDrop_blocked() {
        DataSource ds = h2DataSource();
        when(tenantContext.getTenantDbName()).thenReturn(TENANT_DB);
        when(multiTenantConnectionProvider.getDataSource(TENANT_DB)).thenReturn(ds);
        when(messageSource.getMessage(eq("impex.statement.blocked"), any(), any()))
                .thenReturn("blocked");

        ImpExResult result = service.execute(
                MARKER + "\n/* hide */DROP TABLE products;",
                Locale.ENGLISH, CLIENT_IP);

        assertThat(result.status()).isEqualTo("PARTIAL");
        assertThat(result.failedStatements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Leading whitespace + DROP → blocked (word-boundary keyword check)")
    void leadingWhitespace_drop_blocked() {
        DataSource ds = h2DataSource();
        when(tenantContext.getTenantDbName()).thenReturn(TENANT_DB);
        when(multiTenantConnectionProvider.getDataSource(TENANT_DB)).thenReturn(ds);
        when(messageSource.getMessage(eq("impex.statement.blocked"), any(), any()))
                .thenReturn("blocked");

        ImpExResult result = service.execute(
                MARKER + "\n   \t  DROP TABLE products;",
                Locale.ENGLISH, CLIENT_IP);

        assertThat(result.status()).isEqualTo("PARTIAL");
        assertThat(result.failedStatements()).isEqualTo(1);
    }

    @Test
    @DisplayName("'DROP' as a value inside string literal → INSERT is accepted")
    void dropInsideStringLiteral_accepted() {
        DataSource ds = h2DataSource();
        when(tenantContext.getTenantDbName()).thenReturn(TENANT_DB);
        when(multiTenantConnectionProvider.getDataSource(TENANT_DB)).thenReturn(ds);

        ImpExResult result = service.execute(
                MARKER + "\nINSERT INTO products (id, name) VALUES (1, 'please do not DROP this');",
                Locale.ENGLISH, CLIENT_IP);

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.executedStatements()).isEqualTo(1);
    }

    // ---------- semicolon inside block comment ----------

    @Test
    @DisplayName("Semicolon inside block comment doesn't split statement")
    void semicolonInsideBlockComment_doesNotSplit() {
        DataSource ds = h2DataSource();
        when(tenantContext.getTenantDbName()).thenReturn(TENANT_DB);
        when(multiTenantConnectionProvider.getDataSource(TENANT_DB)).thenReturn(ds);

        ImpExResult result = service.execute(
                MARKER + "\nINSERT INTO products /* a;b;c */ (id, name) VALUES (1, 'X');",
                Locale.ENGLISH, CLIENT_IP);

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.totalStatements()).isEqualTo(1);
    }

    // ---------- inline -- comment ----------

    @Test
    @DisplayName("Inline `-- comment\\n` is stripped mid-statement")
    void inlineLineComment_stripped() {
        DataSource ds = h2DataSource();
        when(tenantContext.getTenantDbName()).thenReturn(TENANT_DB);
        when(multiTenantConnectionProvider.getDataSource(TENANT_DB)).thenReturn(ds);

        ImpExResult result = service.execute(
                MARKER + "\nINSERT INTO products -- inline comment\n(id, name) VALUES (1, 'X');",
                Locale.ENGLISH, CLIENT_IP);

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.executedStatements()).isEqualTo(1);
    }

    // ---------- only comments → no statements ----------

    @Test
    @DisplayName("Script that is only comments parses to 0 executable statements")
    void onlyComments_zeroStatements() {
        DataSource ds = h2DataSource();
        when(tenantContext.getTenantDbName()).thenReturn(TENANT_DB);
        when(multiTenantConnectionProvider.getDataSource(TENANT_DB)).thenReturn(ds);

        ImpExResult result = service.execute(
                MARKER + "\n/* nothing here */;\n-- only a line comment\n",
                Locale.ENGLISH, CLIENT_IP);

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.totalStatements()).isZero();
    }

    private DataSource h2DataSource() {
        DataSource ds = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .build();
        new org.springframework.jdbc.core.JdbcTemplate(ds)
                .execute("CREATE TABLE products (id INT PRIMARY KEY, name VARCHAR(100))");
        return ds;
    }
}
