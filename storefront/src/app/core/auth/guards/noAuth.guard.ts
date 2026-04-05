import { inject } from '@angular/core';
import { CanActivateChildFn, CanActivateFn, Router } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { SUPER_ADMIN_ROLE } from '@shared/constants';
import { AuthService } from 'app/core/auth/auth.service';
import { getAuthenticatedRedirectUrl } from 'app/core/auth/auth.redirect.helper';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { UserService } from 'app/core/user/user.service';
import { of, switchMap } from 'rxjs';

function normalizeSubdomain(value: string | null | undefined): string {
    return (value ?? '').trim().toLowerCase();
}

export const NoAuthGuard: CanActivateFn | CanActivateChildFn = (
    route,
    state
) => {
    const router = inject(Router);
    const authService = inject(AuthService);
    const userService = inject(UserService);
    const translocoService = inject(TranslocoService);
    const tenantContext = inject(TenantContextService);

    return authService.check().pipe(
        switchMap((authenticated) => {
            if (!authenticated) {
                return of(true);
            }

            const querySub = route.queryParamMap.get('subdomain');
            const queryNorm = normalizeSubdomain(querySub);
            const storedNorm = normalizeSubdomain(
                tenantContext.getCurrentSubdomain()
            );

            if (
                queryNorm &&
                storedNorm &&
                queryNorm !== storedNorm
            ) {
                return of(true);
            }

            const user = userService.user();
            if (queryNorm && user?.role === SUPER_ADMIN_ROLE) {
                return of(true);
            }

            return of(
                getAuthenticatedRedirectUrl(
                    router,
                    userService,
                    translocoService
                )
            );
        })
    );
};
