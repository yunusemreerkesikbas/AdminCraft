import { Injectable } from '@angular/core';
import { EndpointKey } from '@modules/admin/api-endpoints';
import { CrudEndpoints } from '@core/crud';
import { BaseMailSubscriberAdminService } from './base-mail-subscriber-admin.service';

@Injectable({ providedIn: 'root' })
export class PlatformMailSubscriberAdminService extends BaseMailSubscriberAdminService {
    protected override readonly exportEndpointKey: EndpointKey = 'platformMailSubscribersExport';

    protected override endpoints: CrudEndpoints = {
        list: 'platformMailSubscribersAdmin',
        getById: 'platformMailSubscriberAdminById',
        create: 'platformMailSubscribersAdmin',
        update: 'platformMailSubscriberAdminById',
        delete: 'platformMailSubscriberAdminById',
    };
}
