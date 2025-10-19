import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud';
import { Observable, interval, switchMap, takeWhile } from 'rxjs';
import { TenantModule } from 'app/core/tenant/tenant.types';
import {
    CreateTenantRequest,
    ProvisionLanguagesRequest,
    ProvisioningJobDto,
    Tenant,
    TenantLanguagesDto,
    UpdateTenantLanguagesRequest,
    UpdateTenantRequest
} from './tenants.types';

@Injectable({ providedIn: 'root' })
export class TenantsService extends CrudHttpService<Tenant, CreateTenantRequest, UpdateTenantRequest> {
    protected override endpoints: CrudEndpoints = {
        list: 'tenants',
        getById: 'tenantById',
        create: 'tenants',
        update: 'tenantById',
        delete: 'tenantById'
    };

    

    getTenantLanguages(tenantId: number): Observable<TenantLanguagesDto> {
        return this.customGet<TenantLanguagesDto>('tenantLanguages', { tenantId });
    }

    updateTenantLanguages(tenantId: number, req: UpdateTenantLanguagesRequest): Observable<TenantLanguagesDto> {
        return this.customPut<TenantLanguagesDto>('tenantLanguages', req, { tenantId });
    }

    provisionLanguages(tenantId: number, req: ProvisionLanguagesRequest): Observable<ProvisioningJobDto> {
        return this.customPost<ProvisioningJobDto>('tenantLanguagesProvision', req, { tenantId });
    }

    getProvisioningJob(jobUuid: string): Observable<ProvisioningJobDto> {
        return this.customGet<ProvisioningJobDto>('provisioningJob', { jobUuid });
    }

    pollProvisioningJob(jobUuid: string, intervalMs: number = 2000): Observable<ProvisioningJobDto> {
        return interval(intervalMs).pipe(
            switchMap(() => this.getProvisioningJob(jobUuid)),
            takeWhile((job) => job.status === 'PENDING' || job.status === 'RUNNING', true)
        );
    }

    getLanguageProvisioningJobStatus(jobUuid: string): Observable<ProvisioningJobDto> {
        return this.getProvisioningJob(jobUuid);
    }

    getAllTenants(): Observable<Tenant[]> {
        return this.list();
    }

    getTenantModules(tenantId: number): Observable<TenantModule[]> {
        return this.customGet<TenantModule[]>('tenantModules', { tenantId });
    }
}