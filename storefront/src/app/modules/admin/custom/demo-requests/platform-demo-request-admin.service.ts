import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud';
import { PlatformDemoRequestRow } from './demo-request.types';

@Injectable({ providedIn: 'root' })
export class PlatformDemoRequestAdminService extends CrudHttpService<
    PlatformDemoRequestRow,
    Partial<PlatformDemoRequestRow>,
    Partial<PlatformDemoRequestRow>
> {
    protected endpoints: CrudEndpoints = {
        list: 'platformDemoRequests',
        getById: 'platformDemoRequestById',
        create: 'platformDemoRequests',
        update: 'platformDemoRequestById',
        delete: 'platformDemoRequestById',
    };
}
