import { Injectable } from '@angular/core';
import { CrudStore } from '@core/crud/crud-store';
import { Tenant } from './tenants.types';

@Injectable({ providedIn: 'root' })
export class TenantStore extends CrudStore<Tenant> {
    constructor() {
        super();
    }
}
