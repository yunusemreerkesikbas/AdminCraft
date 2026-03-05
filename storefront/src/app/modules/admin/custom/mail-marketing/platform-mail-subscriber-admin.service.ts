import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud';
import {
    MailSubscriberAdminVm,
    UpsertMailSubscriberPayload,
} from './mail-marketing.types';

@Injectable({ providedIn: 'root' })
export class PlatformMailSubscriberAdminService extends CrudHttpService<
    MailSubscriberAdminVm,
    UpsertMailSubscriberPayload,
    UpsertMailSubscriberPayload
> {
    protected endpoints: CrudEndpoints = {
        list: 'platformMailSubscribersAdmin',
        getById: 'platformMailSubscriberAdminById',
        create: 'platformMailSubscribersAdmin',
        update: 'platformMailSubscriberAdminById',
        delete: 'platformMailSubscriberAdminById',
    };
}
