import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { UserService } from 'app/core/user/user.service';
import { EMPTY } from 'rxjs';

const PLATFORM_ENDPOINTS: readonly string[] = ['/api/tenants', '/api/provisioning', '/actuator'] as const;

export const tenantInterceptor: HttpInterceptorFn = (req, next) => {
    const tenantContext = inject(TenantContextService);
    const userService = inject(UserService);
    const router = inject(Router);
    const snackBar = inject(MatSnackBar);
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
        snackBar.open('Please select a tenant to continue', 'Close', {
            duration: 5000,
            horizontalPosition: 'center',
            verticalPosition: 'top'
        });
        router.navigate(['/dashboards']);
        return EMPTY;
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


