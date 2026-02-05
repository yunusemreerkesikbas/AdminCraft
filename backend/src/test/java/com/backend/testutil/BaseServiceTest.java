package com.backend.testutil;

import com.backend.domain.repository.TenantRepository;
import com.backend.infrastructure.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Base test class for service unit tests.
 * Provides common setup for TenantContext and Mockito extension.
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseServiceTest {

    protected static final String TEST_TENANT_ID = "1";
    protected static final String TEST_TENANT_DB_NAME = "ac_tenant_1";
    protected static final Long TEST_USER_ID = 100L;

    @Mock
    protected TenantRepository tenantRepository;

    private TenantContext tenantContext;

    @BeforeEach
    void setUpTenantContext() {
        tenantContext = new TenantContext(tenantRepository);
        tenantContext.setTenantId(TEST_TENANT_ID);
        tenantContext.setTenantDbName(TEST_TENANT_DB_NAME);
        tenantContext.setSubdomain("test-tenant");

        // Set static ThreadLocal values for TenantContext.validateActive()
        setStaticTenantContext(TEST_TENANT_ID, TEST_TENANT_DB_NAME);
    }

    @AfterEach
    void clearTenantContext() {
        if (tenantContext != null) {
            tenantContext.clear();
        }
        clearStaticTenantContext();
    }

    /**
     * Sets the static ThreadLocal values in TenantContext for validateActive() to pass.
     */
    protected void setStaticTenantContext(String tenantId, String tenantDbName) {
        TenantContext ctx = new TenantContext(tenantRepository);
        ctx.setTenantId(tenantId);
        ctx.setTenantDbName(tenantDbName);
    }

    /**
     * Clears the static ThreadLocal values in TenantContext.
     */
    protected void clearStaticTenantContext() {
        TenantContext ctx = new TenantContext(tenantRepository);
        ctx.clear();
    }

    /**
     * Simulates an inactive tenant context for testing validateActive() failure.
     */
    protected void simulateInactiveTenantContext() {
        clearStaticTenantContext();
    }

    protected TenantContext getTenantContext() {
        return tenantContext;
    }
}
