import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import { NotificationService } from '@shared/notifications/notification.service';
import { UserService } from 'app/core/user/user.service';

export const moduleGuard: CanActivateFn = (route: ActivatedRouteSnapshot, state) => {
    const userService = inject(UserService);
    const router = inject(Router);
    const notify = inject(NotificationService);
    const requiredModule = route.data?.['requiredModule'] as string | undefined;

    if (!requiredModule) {
        return true;
    }

    const userModules = userService.tenantModules();
    if (userModules.includes(requiredModule)) {
        return true;
    }
    notify.alert(
        `This feature requires the "${requiredModule}" module. Please contact your administrator.`,
        { durationMs: 5000 }
    );
    const currentLang = route.paramMap.get('lang') || 'tr';
    router.navigate([`/${currentLang}/pages`]);

    return false;
};
