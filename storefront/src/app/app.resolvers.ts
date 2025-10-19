import { inject } from '@angular/core';
import { ActivatedRouteSnapshot } from '@angular/router';
import { NavigationService } from 'app/core/navigation/navigation.service';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { MessagesService } from 'app/layout/common/messages/messages.service';
import { NotificationsService } from 'app/layout/common/notifications/notifications.service';
import { ShortcutsService } from 'app/layout/common/shortcuts/shortcuts.service';
import { forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';

export const initialDataResolver = () => {
    const messagesService = inject(MessagesService);
    const navigationService = inject(NavigationService);
    const notificationsService = inject(NotificationsService);
    const shortcutsService = inject(ShortcutsService);
    return forkJoin([
        navigationService.get(),
        messagesService.getAll(),
        notificationsService.getAll(),
        shortcutsService.getAll(),
    ]).pipe(
        map((data) => {
            return data;
        })
    );
};

export const tenantParamResolver = (route: ActivatedRouteSnapshot) => {
    const tenantContext = inject(TenantContextService);
    const subdomain = tenantContext.extractSubdomainFromHost();
    if (subdomain && subdomain !== 'admin') {
        tenantContext.setSubdomain(subdomain);
    }
    return true;
};
