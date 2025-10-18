import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';

export const tenantInterceptor: HttpInterceptorFn = (req, next) => {
    const tenantContext = inject(TenantContextService);
    const subdomain = tenantContext.getCurrentSubdomain();
    const tenantId = tenantContext.getCurrentTenantId();

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


