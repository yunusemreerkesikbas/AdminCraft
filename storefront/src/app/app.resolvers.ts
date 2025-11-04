import { inject } from '@angular/core';
import { MessagesService } from 'app/layout/common/messages/messages.service';
import { NotificationsService } from 'app/layout/common/notifications/notifications.service';
import { ShortcutsService } from 'app/layout/common/shortcuts/shortcuts.service';
import { forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';

export const initialDataResolver = () => {
    const messagesService = inject(MessagesService);
    const notificationsService = inject(NotificationsService);
    const shortcutsService = inject(ShortcutsService);
    return forkJoin([
        messagesService.getAll(),
        notificationsService.getAll(),
        shortcutsService.getAll(),
    ]).pipe(
        map((data) => {
            return data;
        })
    );
};
