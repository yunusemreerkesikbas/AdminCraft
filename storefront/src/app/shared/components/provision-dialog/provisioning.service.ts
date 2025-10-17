import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud';
import { Observable } from 'rxjs';
import { ModuleCatalog, ProvisioningJob } from './provision.types';

export interface ApiResponse<T> {
    result: string;
    message: string;
    data: T;
}

export interface ProvisionRequest {
    modules: string[];
}

@Injectable({
    providedIn: 'root'
})
export class ProvisioningService extends CrudHttpService<any, any, any> {
    protected endpoints: CrudEndpoints = {
        list: 'provisioningModulesCatalog',
        getById: 'provisioningJob',
        create: 'provisioningTenantProvision',
        update: 'provisioningJob',
        delete: 'provisioningJob'
    };

    getModulesCatalog(): Observable<ApiResponse<ModuleCatalog[]>> {
        return this.customGet<ApiResponse<ModuleCatalog[]>>('provisioningModulesCatalog');
    }

    provisionTenant(tenantId: number, request: ProvisionRequest): Observable<ApiResponse<ProvisioningJob>> {
        return this.customPost<ApiResponse<ProvisioningJob>>(
            'provisioningTenantProvision',
            request,
            { tenantId }
        );
    }

    getJobStatus(jobId: number): Observable<ApiResponse<ProvisioningJob>> {
        return this.customGet<ApiResponse<ProvisioningJob>>(
            'provisioningJob',
            { jobId }
        );
    }
}
