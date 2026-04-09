import { Injectable } from '@angular/core';
import { EndpointKey } from '@modules/admin/api-endpoints';
import { CrudEndpoints } from '@core/crud';
import { BaseMailSubscriberAdminService } from './base-mail-subscriber-admin.service';

@Injectable({ providedIn: 'root' })
export class TenantMailSubscriberAdminService extends BaseMailSubscriberAdminService {
    protected override readonly exportEndpointKey: EndpointKey = 'mailSubscribersExport';

    protected override endpoints: CrudEndpoints = {
        list: 'mailSubscribersAdmin',
        getById: 'mailSubscriberAdminById',
        create: 'mailSubscribersAdmin',
        update: 'mailSubscriberAdminById',
        delete: 'mailSubscriberAdminById',
    };
}
