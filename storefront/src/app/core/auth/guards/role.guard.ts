import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import {
    SUPER_ADMIN_ROLE,
    TENANT_ADMIN_ROLE,
    VIEWER_ROLE,
} from '@shared/constants';
import { NotificationService } from '@shared/notifications/notification.service';
import { UserService } from 'app/core/user/user.service';

export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
    return (route: ActivatedRouteSnapshot) => {
        const userService = inject(UserService);
        const router = inject(Router);
        const notify = inject(NotificationService);
        const user = userService.user();
        const userRole = user?.role;

        if (!userRole || !allowedRoles.includes(userRole)) {
            notify.alert('You do not have permission to access this page', {
                durationMs: 5000,
            });

            router.navigateByUrl('/sign-in');
            return false;
        }

        return true;
    };
};

export const superAdminGuard: CanActivateFn = roleGuard([SUPER_ADMIN_ROLE]);
export const tenantAdminGuard: CanActivateFn = roleGuard([TENANT_ADMIN_ROLE]);
export const tenantUserGuard: CanActivateFn = roleGuard([
    TENANT_ADMIN_ROLE,
    VIEWER_ROLE,
]);
