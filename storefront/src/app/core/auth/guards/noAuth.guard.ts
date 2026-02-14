import { inject } from '@angular/core';
import { CanActivateChildFn, CanActivateFn, Router } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { AuthService } from 'app/core/auth/auth.service';
import { getAuthenticatedRedirectUrl } from 'app/core/auth/auth.redirect.helper';
import { UserService } from 'app/core/user/user.service';
import { of, switchMap } from 'rxjs';

export const NoAuthGuard: CanActivateFn | CanActivateChildFn = (
    route,
    state
) => {
    const router = inject(Router);
    const authService = inject(AuthService);
    const userService = inject(UserService);
    const translocoService = inject(TranslocoService);

    return authService.check().pipe(
        switchMap((authenticated) => {
            if (authenticated) {
                return of(getAuthenticatedRedirectUrl(router, userService, translocoService));
            }

            return of(true);
        })
    );
};
