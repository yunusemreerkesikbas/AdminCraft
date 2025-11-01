import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationService } from '@shared/notifications/notification.service';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { UserService } from 'app/core/user/user.service';
import { EMPTY } from 'rxjs';

const PLATFORM_ENDPOINTS: readonly string[] = ['/api/provisioning', '/actuator'] as const;
const TENANT_SPECIFIC_EXCEPTIONS: readonly string[] = ['/api/tenants/current/modules'] as const;

export const tenantInterceptor: HttpInterceptorFn = (req, next) => {
    const tenantContext = inject(TenantContextService);
    const userService = inject(UserService);
    const router = inject(Router);
    const notify = inject(NotificationService);
    const isTenantSpecificException = TENANT_SPECIFIC_EXCEPTIONS.some((endpoint) =>
        req.url.includes(endpoint)
    );
    if (!isTenantSpecificException) {
        const isPlatformEndpoint = PLATFORM_ENDPOINTS.some((endpoint) => req.url.includes(endpoint)) ||
            req.url.includes('/api/tenants');

        if (isPlatformEndpoint) {
            return next(req);
        }
    }
    const user = userService.user();
    const subdomain = tenantContext.getCurrentSubdomain();
    const contextTenantId = tenantContext.getCurrentTenantId();
    const effectiveTenantId = user?.role === 'TENANT_ADMIN' && user?.tenantId
        ? user.tenantId
        : contextTenantId;

    if (user?.role === 'SUPER_ADMIN' && !effectiveTenantId) {
        notify.alert('Please select a tenant to continue', { durationMs: 5000 });
        router.navigate(['/dashboards']);
        return EMPTY;
    }
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


