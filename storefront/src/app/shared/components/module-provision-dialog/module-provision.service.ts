import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud';
import { Observable } from 'rxjs';
import { ApiResponse, ModuleCatalog, ProvisioningJob, ProvisionRequest } from './module-provision.types';

@Injectable({
    providedIn: 'root'
})
export class ModuleProvisionService extends CrudHttpService<any, any, any> {
    protected endpoints: CrudEndpoints = {
        list: 'provisioningModulesCatalog',
        getById: 'provisioningJob',
        create: 'provisioningTenantProvision',
        update: 'provisioningJob',
        delete: 'provisioningJob'
    };

    getModulesCatalog(): Observable<ApiResponse<ModuleCatalog[]>> {
        return this.api.get<ApiResponse<ModuleCatalog[]>>('provisioningModulesCatalog');
    }

    provisionTenant(tenantId: number, request: ProvisionRequest): Observable<ApiResponse<ProvisioningJob>> {
        return this.api.post<ApiResponse<ProvisioningJob>>(
            'provisioningTenantProvision',
            request,
            { tenantId }
        );
    }

    getJobStatus(jobId: number): Observable<ApiResponse<ProvisioningJob>> {
        return this.api.get<ApiResponse<ProvisioningJob>>(
            'provisioningJob',
            { jobId }
        );
    }
}
