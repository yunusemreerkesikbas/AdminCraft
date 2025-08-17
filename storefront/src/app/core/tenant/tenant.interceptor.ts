import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';

export const tenantInterceptor: HttpInterceptorFn = (req, next) => {
    const tenantContext = inject(TenantContextService);
    const sub = tenantContext.getCurrentSubdomain();

    if (!sub) {
        return next(req);
    }

    const cloned = req.clone({
        setHeaders: { 'X-Tenant-Subdomain': sub },
    });

    return next(cloned);
};


