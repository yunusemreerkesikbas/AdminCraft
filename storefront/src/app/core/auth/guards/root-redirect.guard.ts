import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { AuthService } from 'app/core/auth/auth.service';
import { UserService } from 'app/core/user/user.service';
import { of, switchMap } from 'rxjs';

export const rootRedirectGuard: CanActivateFn = () => {
    const router = inject(Router);
    const authService = inject(AuthService);
    const userService = inject(UserService);
    const translocoService = inject(TranslocoService);

    return authService.check().pipe(
        switchMap((authenticated) => {
            const lang = translocoService.getActiveLang();

            if (!authenticated) {
                return of(router.parseUrl('/sign-in'));
            }

            const user = userService.user();
            
            if (user?.role === 'SUPER_ADMIN') {
                return of(router.parseUrl(`/${lang}/tenants`));
            }

            return of(router.parseUrl(`/${lang}/site`));
        })
    );
};
