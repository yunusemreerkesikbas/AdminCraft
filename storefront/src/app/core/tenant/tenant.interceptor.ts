import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { UserService } from 'app/core/user/user.service';
import { throwError } from 'rxjs';

export const tenantInterceptor: HttpInterceptorFn = (req, next) => {
    const tenantContext = inject(TenantContextService);
    const userService = inject(UserService);
    const platformEndpoints = ['/api/tenants', '/api/provisioning', '/actuator', '/api/auth'];
    const isPlatformEndpoint = platformEndpoints.some((endpoint) =>
        req.url.includes(endpoint)
    );
    if (isPlatformEndpoint) {
        return next(req);
    }
    const user = userService.user;
    const subdomain = tenantContext.getCurrentSubdomain();
    const tenantId = tenantContext.getCurrentTenantId();

    if (user?.role === 'SUPER_ADMIN' && !tenantId) {
        console.warn('[TenantInterceptor] No tenant selected, request blocked:', req.url);
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


