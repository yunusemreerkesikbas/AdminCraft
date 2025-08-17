import { inject } from '@angular/core';
import { NavigationService } from 'app/core/navigation/navigation.service';
import { MessagesService } from 'app/layout/common/messages/messages.service';
import { NotificationsService } from 'app/layout/common/notifications/notifications.service';
import { ShortcutsService } from 'app/layout/common/shortcuts/shortcuts.service';
import { forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

export const initialDataResolver = () => {
    const messagesService = inject(MessagesService);
    const navigationService = inject(NavigationService);
    const notificationsService = inject(NotificationsService);
    const shortcutsService = inject(ShortcutsService);

    // Fork join multiple API endpoint calls to wait all of them to finish
    return forkJoin([
        navigationService.get(),
        messagesService.getAll(),
        notificationsService.getAll(),
        shortcutsService.getAll(),
    ]).pipe(
        map((data) => {
            // ensure placeholder links are replaced based on current tenant
            // note: replacement is also done in mock API; this is a noop here
            // kept for completeness if backend supplies links in future
            return data;
        })
    );
};

export const tenantParamResolver = (route: ActivatedRouteSnapshot) => {
    const tenantContext = inject(TenantContextService);
    const router = inject(Router);
    const t = route.paramMap.get('tenant');
    if (!t) { return true; }
    const isValid = /^[a-z0-9-]{1,50}$/.test(t);
    if (!isValid) {
        router.navigate(['404-not-found']);
        return false;
    }
    tenantContext.setSubdomain(t);
    return true;
};
