import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { AuthService } from 'app/core/auth/auth.service';
import { getAuthenticatedRedirectUrl } from 'app/core/auth/auth.redirect.helper';
import { UserService } from 'app/core/user/user.service';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { SUPER_ADMIN_ROLE } from '@shared/constants';
import { of, switchMap } from 'rxjs';

export const rootRedirectGuard: CanActivateFn = () => {
    const router = inject(Router);
    const authService = inject(AuthService);
    const userService = inject(UserService);
    const translocoService = inject(TranslocoService);
    const tenantContext = inject(TenantContextService);

    const params = new URLSearchParams(window.location.search);
    const subdomain = params.get('subdomain');
    if (subdomain) {
        return of(router.parseUrl(`/sign-in?subdomain=${encodeURIComponent(subdomain)}`));
    }

    return authService.check().pipe(
        switchMap((authenticated) => {
            if (!authenticated) {
                return of(router.parseUrl('/sign-in'));
            }

            const hostnameSubdomain = tenantContext.extractSubdomainFromHost();
            const isPlatformAdminUrl = hostnameSubdomain === 'admin';
            const user = userService.user();

            if (isPlatformAdminUrl && user?.role !== SUPER_ADMIN_ROLE) {
                return of(router.parseUrl('/sign-in'));
            }

            return of(getAuthenticatedRedirectUrl(router, userService, translocoService));
        })
    );
};
