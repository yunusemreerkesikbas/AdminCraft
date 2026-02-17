import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud';
import { TenantModule } from 'app/core/tenant/tenant.types';
import { Observable, map } from 'rxjs';
import { ApiResponse } from '@core/crud/api.types';
import {
    AdminUserResponse,
    CreateTenantRequest,
    ProvisionLanguagesRequest,
    ProvisioningJobDto,
    ProvisioningJobResponse,
    SyncJobDto,
    Tenant,
    TenantDetailResponse,
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

    getProvisioningJobById(jobId: number): Observable<SyncJobDto> {
        return this.customGet<SyncJobDto>('provisioningJob', { jobId });
    }

    getLanguageProvisioningJobStatus(jobUuid: string): Observable<ProvisioningJobDto> {
        return this.getProvisioningJob(jobUuid);
    }

    getAllTenants(): Observable<Tenant[]> {
        return this.listPaged({ page: 0, size: 500, sort: 'companyName,asc' }).pipe(
            map((page) => page.content || [])
        );
    }

    createWithResponse(
        request: CreateTenantRequest
    ): Observable<ApiResponse<Tenant>> {
        return this.api.post<ApiResponse<Tenant>>(this.endpoints.create, request);
    }

    updateWithResponse(
        id: number,
        request: UpdateTenantRequest
    ): Observable<ApiResponse<Tenant>> {
        return this.api.put<ApiResponse<Tenant>>(this.endpoints.update, request, {
            id,
        });
    }

    getTenantModules(tenantId: number): Observable<TenantModule[]> {
        return this.customGet<TenantModule[]>('tenantModules', { tenantId });
    }

    generateAdminUser(tenantId: number): Observable<AdminUserResponse> {
        return this.customPost<AdminUserResponse>('generateAdminUser', {}, { tenantId });
    }

    syncMigrations(tenantId: number): Observable<ApiResponse<SyncJobDto>> {
        return this.api.post<ApiResponse<SyncJobDto>>(
            'provisioningSyncMigrations',
            {},
            { tenantId }
        );
    }

    getTenantDetail(tenantId: number): Observable<TenantDetailResponse> {
        return this.customGet<TenantDetailResponse>('tenantById', { id: tenantId });
    }

    getTenantProvisioningJobs(tenantId: number): Observable<ProvisioningJobResponse[]> {
        return this.customGet<ProvisioningJobResponse[]>('tenantProvisioningJobs', { tenantId });
    }
}
