import { Router, UrlTree } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { UserService } from 'app/core/user/user.service';
import { SUPER_ADMIN_ROLE } from '@shared/constants';

export function getAuthenticatedRedirectUrl(
    router: Router,
    userService: UserService,
    translocoService: TranslocoService
): UrlTree {
    const lang = translocoService.getActiveLang();
    const user = userService.user();

    if (!user) {
        return router.parseUrl('/sign-in');
    }

    if (user.role === SUPER_ADMIN_ROLE) {
        return router.parseUrl(`/${lang}/tenants`);
    }

    return router.parseUrl(`/${lang}/site`);
}
