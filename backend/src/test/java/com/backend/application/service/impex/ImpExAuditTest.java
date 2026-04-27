package com.backend.application.service.impex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.backend.application.dto.impex.ImpExResult;
import com.backend.domain.entity.ImpExAudit;
import com.backend.domain.exception.ImpExInvalidScriptException;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.ImpExAuditRepository;
import com.backend.infrastructure.tenant.MultiTenantConnectionProvider;
import com.backend.shared.common.SecurityHelper;

@ExtendWith(MockitoExtension.class)
class ImpExAuditTest {

    private static final String MARKER = "-- #CRAFTIVE_IMPEX";
    private static final String CLIENT_IP = "10.0.0.7";
    private static final String CORRELATION_ID = "corr-abc-123";
    private static final String TENANT_DB = "ac_demo_42";

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
    @DisplayName("REJECTED — marker missing: audit row written with status=REJECTED, total=0")
    void rejected_MarkerMissing_StillWritesAudit() {
        when(messageSource.getMessage(eq("impex.error.marker.missing"), any(), any())).thenReturn("marker missing");

        try {
            service.execute("INSERT INTO products (id) VALUES (1);", Locale.ENGLISH, CLIENT_IP);
        } catch (ImpExInvalidScriptException ignored) {
            // expected
        }

        ImpExAudit captured = captureAudit();
        assertThat(captured.getStatus()).isEqualTo("REJECTED");
        assertThat(captured.getStatementCount()).isZero();
        assertThat(captured.getSuccessCount()).isZero();
        assertThat(captured.getFailedCount()).isZero();
        assertThat(captured.getClientIp()).isEqualTo(CLIENT_IP);
        assertThat(captured.getCorrelationId()).isEqualTo(CORRELATION_ID);
    }

    @Test
    @DisplayName("REJECTED — tenant context missing: marker valid but no tenant; audit yine yazılır")
    void rejected_TenantMissing_StillWritesAudit() {
        when(tenantContext.getTenantDbName()).thenReturn(null);
        when(messageSource.getMessage(eq("impex.error.tenant.required"), any(), any())).thenReturn("tenant required");

        try {
            service.execute(MARKER + "\nINSERT INTO products (id) VALUES (1);", Locale.ENGLISH, CLIENT_IP);
        } catch (IllegalStateException ignored) {
            // expected
        }

        ImpExAudit captured = captureAudit();
        assertThat(captured.getStatus()).isEqualTo("REJECTED");
        assertThat(captured.getStatementCount()).isEqualTo(1);
        assertThat(captured.getTenantDb()).isNull();
    }

    @Test
    @DisplayName("Audit insert exception does NOT break service flow")
    void auditInsertFailure_DoesNotBreakFlow() {
        when(messageSource.getMessage(eq("impex.error.marker.missing"), any(), any())).thenReturn("marker missing");
        doThrow(new RuntimeException("DB down")).when(impExAuditRepository).save(any(ImpExAudit.class));

        boolean threwExpected = false;
        try {
            service.execute("INSERT INTO products (id) VALUES (1);", Locale.ENGLISH, CLIENT_IP);
        } catch (ImpExInvalidScriptException ex) {
            threwExpected = true;
        }

        assertThat(threwExpected).as("Original exception still propagates").isTrue();
        verify(impExAuditRepository, times(1)).save(any(ImpExAudit.class));
    }

    @Test
    @DisplayName("full_sql is truncated to MAX_AUDIT_SQL_LENGTH (1 MB)")
    void fullSqlExceedingCap_TruncatedTo1Mb() {
        when(messageSource.getMessage(eq("impex.error.marker.missing"), any(), any())).thenReturn("marker missing");
        String huge = "X".repeat(ImpExServiceImpl.MAX_AUDIT_SQL_LENGTH + 1024);

        try {
            service.execute(huge, Locale.ENGLISH, CLIENT_IP);
        } catch (ImpExInvalidScriptException ignored) {
        }

        ImpExAudit captured = captureAudit();
        assertThat(captured.getFullSql()).hasSize(ImpExServiceImpl.MAX_AUDIT_SQL_LENGTH);
    }

    @Test
    @DisplayName("clientIp + correlationId propagation: values from controller + MDC end up in audit row")
    void clientIpAndCorrelationId_Propagated() {
        when(messageSource.getMessage(eq("impex.error.marker.missing"), any(), any())).thenReturn("marker missing");
        when(securityHelper.getCurrentUserIdOrNull()).thenReturn(99L);
        when(securityHelper.getCurrentUserEmail()).thenReturn("admin@example.com");
        when(securityHelper.isSuperAdmin()).thenReturn(true);
        when(tenantContext.getTenantId()).thenReturn("42");
        when(tenantContext.getTenantDbName()).thenReturn(TENANT_DB);

        try {
            service.execute("noop;", Locale.ENGLISH, CLIENT_IP);
        } catch (ImpExInvalidScriptException ignored) {
        }

        ImpExAudit captured = captureAudit();
        assertThat(captured.getClientIp()).isEqualTo(CLIENT_IP);
        assertThat(captured.getCorrelationId()).isEqualTo(CORRELATION_ID);
        assertThat(captured.getActorUserId()).isEqualTo(99L);
        assertThat(captured.getActorEmail()).isEqualTo("admin@example.com");
        assertThat(captured.getActorRole()).isEqualTo("SUPER_ADMIN");
        assertThat(captured.getTenantId()).isEqualTo(42L);
        assertThat(captured.getTenantDb()).isEqualTo(TENANT_DB);
    }

    @Test
    @DisplayName("SUCCESS — all statements succeed: audit status=SUCCESS")
    void success_AllStatementsExecuted() {
        DataSource ds = h2DataSource();
        when(tenantContext.getTenantDbName()).thenReturn(TENANT_DB);
        when(multiTenantConnectionProvider.getDataSource(TENANT_DB)).thenReturn(ds);

        ImpExResult result = service.execute(
                MARKER + "\nINSERT INTO products (id, name) VALUES (1, 'A');",
                Locale.ENGLISH, CLIENT_IP);

        assertThat(result.status()).isEqualTo("SUCCESS");
        ImpExAudit captured = captureAudit();
        assertThat(captured.getStatus()).isEqualTo("SUCCESS");
        assertThat(captured.getStatementCount()).isEqualTo(1);
        assertThat(captured.getSuccessCount()).isEqualTo(1);
        assertThat(captured.getFailedCount()).isZero();
    }

    @Test
    @DisplayName("PARTIAL — one OK + one fail (sensitive table reject): audit status=PARTIAL")
    void partial_MixedSuccessFailure() {
        DataSource ds = h2DataSource();
        when(tenantContext.getTenantDbName()).thenReturn(TENANT_DB);
        when(multiTenantConnectionProvider.getDataSource(TENANT_DB)).thenReturn(ds);
        when(messageSource.getMessage(eq("impex.error.sensitive.table"), any(), any())).thenReturn("sensitive table");

        ImpExResult result = service.execute(
                MARKER + "\nINSERT INTO products (id, name) VALUES (2, 'B');"
                        + "\nUPDATE users SET role='SUPER_ADMIN' WHERE id=1;",
                Locale.ENGLISH, CLIENT_IP);

        assertThat(result.status()).isEqualTo("PARTIAL");
        ImpExAudit captured = captureAudit();
        assertThat(captured.getStatus()).isEqualTo("PARTIAL");
        assertThat(captured.getStatementCount()).isEqualTo(2);
        assertThat(captured.getSuccessCount()).isEqualTo(1);
        assertThat(captured.getFailedCount()).isEqualTo(1);
    }

    private ImpExAudit captureAudit() {
        ArgumentCaptor<ImpExAudit> captor = ArgumentCaptor.forClass(ImpExAudit.class);
        verify(impExAuditRepository, times(1)).save(captor.capture());
        return captor.getValue();
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
