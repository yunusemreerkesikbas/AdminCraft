import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { UserService } from 'app/core/user/user.service';
import { throwError } from 'rxjs';

// Platform endpoints that don't require tenant context
// Note: /api/auth/login NEEDS X-Tenant-Subdomain for Sprint 17 subdomain-based auth
const PLATFORM_ENDPOINTS: readonly string[] = ['/api/tenants', '/api/provisioning', '/actuator'] as const;

export const tenantInterceptor: HttpInterceptorFn = (req, next) => {
    const tenantContext = inject(TenantContextService);
    const userService = inject(UserService);

    // Check if this is a platform-only endpoint
    const isPlatformEndpoint = PLATFORM_ENDPOINTS.some((endpoint) =>
        req.url.includes(endpoint)
    );
    if (isPlatformEndpoint) {
        return next(req);
    }
    const user = userService.user;
    const subdomain = tenantContext.getCurrentSubdomain();
    const tenantId = tenantContext.getCurrentTenantId();

    if (user?.role === 'SUPER_ADMIN' && !tenantId) {
        return throwError(() => new Error('Please select a tenant to continue'));
    }
    if (!subdomain && tenantId == null) {
        return next(req);
    }

    const headers: Record<string, string> = {};
    if (subdomain) {
        headers['X-Tenant-Subdomain'] = subdomain;
    }
    if (tenantId != null) {
        headers['X-Tenant-ID'] = String(tenantId);
    }

    const cloned = req.clone({ setHeaders: headers });
    return next(cloned);
};


