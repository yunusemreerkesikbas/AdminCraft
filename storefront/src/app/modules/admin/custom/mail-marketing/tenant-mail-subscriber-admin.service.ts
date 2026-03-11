import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud';
import {
    MailSubscriberAdminVm,
    UpsertMailSubscriberPayload,
} from './mail-marketing.types';

@Injectable({ providedIn: 'root' })
export class TenantMailSubscriberAdminService extends CrudHttpService<
    MailSubscriberAdminVm,
    UpsertMailSubscriberPayload,
    UpsertMailSubscriberPayload
> {
    protected endpoints: CrudEndpoints = {
        list: 'mailSubscribersAdmin',
        getById: 'mailSubscriberAdminById',
        create: 'mailSubscribersAdmin',
        update: 'mailSubscriberAdminById',
        delete: 'mailSubscriberAdminById',
    };
}
