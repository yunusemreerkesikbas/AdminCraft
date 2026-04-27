package com.backend.application.service.impex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Locale;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.backend.application.dto.impex.ImpExResult;
import com.backend.application.dto.impex.StatementResult;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.ImpExAuditRepository;
import com.backend.infrastructure.tenant.MultiTenantConnectionProvider;
import com.backend.shared.common.SecurityHelper;

/**
 * SEC-110: ensures the per-statement errorMessage exposed in ImpExResult never carries raw
 * JDBC detail (table names, column names, vendor error codes). Only the i18n category
 * message + correlationId reference reaches the client.
 */
@ExtendWith(MockitoExtension.class)
class ImpExErrorSanitizationTest {

    private static final String MARKER = "-- #CRAFTIVE_IMPEX";
    private static final String TENANT_DB = "ac_demo_42";
    private static final String CLIENT_IP = "10.0.0.7";
    private static final String CORRELATION_ID = "corr-xyz-789";

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
        MDC.put("correlationId", CORRELATION_ID);
    }

    @AfterEach
    void tearDown() {
        MDC.remove("correlationId");
    }

    @Test
    @DisplayName("Syntax error → safe i18n message + (ref: correlationId), no raw JDBC text")
    void syntaxError_sanitized_andRefAppended() {
        DataSource ds = h2DataSource();
        when(tenantContext.getTenantDbName()).thenReturn(TENANT_DB);
        when(multiTenantConnectionProvider.getDataSource(TENANT_DB)).thenReturn(ds);
        when(messageSource.getMessage(eq("impex.error.sql.syntax"), any(), any()))
                .thenReturn("SQL syntax error");

        ImpExResult result = service.execute(
                MARKER + "\nINSERT INTO products (id, name) VALES (1, 'X');",
                Locale.ENGLISH, CLIENT_IP);

        assertThat(result.status()).isEqualTo("PARTIAL");
        StatementResult failed = result.results().get(0);
        assertThat(failed.success()).isFalse();
        assertThat(failed.errorMessage())
                .startsWith("SQL syntax error")
                .endsWith("(ref: " + CORRELATION_ID + ")")
                // raw JDBC fragments must not leak
                .doesNotContainIgnoringCase("VALES")
                .doesNotContainIgnoringCase("syntax error in SQL statement")
                .doesNotContainIgnoringCase("expected");
    }

    @Test
    @DisplayName("Duplicate key (PK conflict) → impex.error.sql.duplicate, no raw 'unique index' text")
    void duplicateKey_sanitized() {
        DataSource ds = h2DataSource();
        when(tenantContext.getTenantDbName()).thenReturn(TENANT_DB);
        when(multiTenantConnectionProvider.getDataSource(TENANT_DB)).thenReturn(ds);
        // accept either duplicate or constraint depending on H2's classification
        lenient().when(messageSource.getMessage(eq("impex.error.sql.duplicate"), any(), any()))
                .thenReturn("Duplicate key");
        lenient().when(messageSource.getMessage(eq("impex.error.sql.constraint"), any(), any()))
                .thenReturn("Constraint violation");

        ImpExResult result = service.execute(
                MARKER
                        + "\nINSERT INTO products (id, name) VALUES (1, 'A');"
                        + "\nINSERT INTO products (id, name) VALUES (1, 'B');",
                Locale.ENGLISH, CLIENT_IP);

        assertThat(result.status()).isEqualTo("PARTIAL");
        assertThat(result.executedStatements()).isEqualTo(1);
        assertThat(result.failedStatements()).isEqualTo(1);

        StatementResult failed = result.results().get(1);
        assertThat(failed.errorMessage())
                .satisfiesAnyOf(
                        msg -> assertThat(msg).startsWith("Duplicate key"),
                        msg -> assertThat(msg).startsWith("Constraint violation"))
                .doesNotContainIgnoringCase("unique index")
                .doesNotContainIgnoringCase("primary key violation")
                .doesNotContainIgnoringCase("PRIMARY_KEY");
    }

    @Test
    @DisplayName("Unknown table error → safe message, no table name leak")
    void unknownTable_sanitized_noLeak() {
        DataSource ds = h2DataSource();
        when(tenantContext.getTenantDbName()).thenReturn(TENANT_DB);
        when(multiTenantConnectionProvider.getDataSource(TENANT_DB)).thenReturn(ds);
        lenient().when(messageSource.getMessage(eq("impex.error.sql.syntax"), any(), any()))
                .thenReturn("SQL syntax error");
        lenient().when(messageSource.getMessage(eq("impex.error.sql.dataAccess"), any(), any()))
                .thenReturn("Database error");

        ImpExResult result = service.execute(
                MARKER + "\nINSERT INTO nonexistent_table_xyz (id) VALUES (1);",
                Locale.ENGLISH, CLIENT_IP);

        StatementResult failed = result.results().get(0);
        assertThat(failed.errorMessage())
                .doesNotContainIgnoringCase("nonexistent_table_xyz")
                .doesNotContainIgnoringCase("table");
    }

    @Test
    @DisplayName("MDC correlationId absent → no `(ref:)` suffix in error message")
    void noCorrelationId_noRefSuffix() {
        MDC.remove("correlationId");
        DataSource ds = h2DataSource();
        when(tenantContext.getTenantDbName()).thenReturn(TENANT_DB);
        when(multiTenantConnectionProvider.getDataSource(TENANT_DB)).thenReturn(ds);
        when(messageSource.getMessage(eq("impex.error.sql.syntax"), any(), any()))
                .thenReturn("SQL syntax error");

        ImpExResult result = service.execute(
                MARKER + "\nINSERT INTO products (id, name) VALES (1, 'X');",
                Locale.ENGLISH, CLIENT_IP);

        StatementResult failed = result.results().get(0);
        assertThat(failed.errorMessage()).doesNotContain("(ref:");
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
