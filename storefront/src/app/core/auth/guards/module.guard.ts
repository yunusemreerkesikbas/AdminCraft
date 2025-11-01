import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import { UserService } from 'app/core/user/user.service';

export const ModuleGuard: CanActivateFn = (route: ActivatedRouteSnapshot, state) => {
    const userService = inject(UserService);
    const router = inject(Router);
    const snackBar = inject(MatSnackBar);
    const requiredModule = route.data?.['requiredModule'] as string | undefined;

    if (!requiredModule) {
        return true;
    }

    const userModules = userService.tenantModules();
    const user = userService.user();
    if (userModules.includes(requiredModule)) {
        return true;
    }
    const moduleName = getModuleDisplayName(requiredModule);
    snackBar.open(
        `This feature requires the "${moduleName}" module. Please contact your administrator.`,
        'Close',
        {
            duration: 5000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['error-snackbar']
        }
    );
    const currentLang = route.paramMap.get('lang') || 'tr';
    router.navigate([`/${currentLang}/dashboards/project`]);

    return false;
};

function getModuleDisplayName(moduleCode: string): string {
    const moduleNames: Record<string, string> = {
        'core': 'Core',
        'media': 'Media Management',
        'pagebuilder': 'Page Builder',
        'site_settings': 'Site Settings',
        'page_categories': 'Page Categories'
    };
    return moduleNames[moduleCode] || moduleCode;
}
