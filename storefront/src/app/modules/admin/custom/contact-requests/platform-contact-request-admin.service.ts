import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud';
import { PlatformContactRequestRow } from './contact-request.types';

@Injectable({ providedIn: 'root' })
export class ContactRequestAdminService extends CrudHttpService<
    PlatformContactRequestRow,
    Partial<PlatformContactRequestRow>,
    Partial<PlatformContactRequestRow>
> {
    protected endpoints: CrudEndpoints = {
        list: 'contactRequests',
        getById: 'contactRequestById',
        create: 'contactRequests',
        update: 'contactRequestById',
        delete: 'contactRequestById',
    };
}
