import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CanActivateFn, Router } from '@angular/router';
import { SUPER_ADMIN_ROLE } from '@shared/constants';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { UserService } from 'app/core/user/user.service';

export const tenantGuard: CanActivateFn = (route, state) => {
    const tenantContext = inject(TenantContextService);
    const userService = inject(UserService);
    const router = inject(Router);
    const snackBar = inject(MatSnackBar);
    const user = userService.user();
    const tenantId = tenantContext.getCurrentTenantId();
    if (user?.role === SUPER_ADMIN_ROLE && !tenantId) {
        snackBar.open('Please select a tenant to access this page', 'Close', {
            duration: 5000,
            horizontalPosition: 'center',
            verticalPosition: 'top'
        });
        router.navigate(['/dashboards']);
        return false;
    }
    return true;
};
