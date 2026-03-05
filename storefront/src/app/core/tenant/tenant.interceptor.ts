import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { UserService } from 'app/core/user/user.service';

const PLATFORM_ENDPOINTS: readonly string[] = [
  'api/provisioning',
  'actuator',
  'api/common/navigation',
  'api/common/user'
] as const;
const TENANT_SPECIFIC_EXCEPTIONS: readonly string[] = [
  'api/tenants/current/modules',
  'api/tenants/current/detail'
] as const;
const AUTH_ENDPOINTS: readonly string[] = [
  'auth/login',
  'auth/refresh',
  'auth/forgotPassword',
  'auth/resetPassword',
  'auth/verifyOtp',
  'auth/setInitialPassword',
  'auth/verify-otp'
] as const;
const CONFIG_ENDPOINTS: readonly string[] = [
  'config/auth',
  'config/admin'
] as const;

export const tenantInterceptor: HttpInterceptorFn = (req, next) => {
    const isConfigEndpoint = CONFIG_ENDPOINTS.some((endpoint) =>
        req.url.includes(endpoint)
    );
    if (isConfigEndpoint) {
        return next(req);
    }

    const tenantContext = inject(TenantContextService);
    const userService = inject(UserService);
    const isTenantSpecificException = TENANT_SPECIFIC_EXCEPTIONS.some((endpoint) =>
        req.url.includes(endpoint)
    );
    if (!isTenantSpecificException) {
        const isPlatformEndpoint = PLATFORM_ENDPOINTS.some((endpoint) => req.url.includes(endpoint)) ||
            req.url.includes('api/tenants');
        if (isPlatformEndpoint) {
            return next(req);
        }
    }
    const user = userService.user();
    let subdomain = tenantContext.getCurrentSubdomain();
    if (!subdomain && AUTH_ENDPOINTS.some((endpoint) => req.url.includes(endpoint))) {
        subdomain = tenantContext.extractSubdomainFromHost();
        if (subdomain === 'admin') subdomain = null;
    }
    const contextTenantId = tenantContext.getCurrentTenantId();
    const effectiveTenantId = user?.role !== 'SUPER_ADMIN' && user?.tenantId
        ? user.tenantId
        : contextTenantId;

    if (!subdomain && effectiveTenantId == null) {
        return next(req);
    }

    const headers: Record<string, string> = {};
    if (subdomain) {
        headers['X-Tenant-Subdomain'] = subdomain;
    }
    if (effectiveTenantId != null) {
        headers['X-Tenant-ID'] = String(effectiveTenantId);
    }

    const cloned = req.clone({ setHeaders: headers });
    return next(cloned);
};
