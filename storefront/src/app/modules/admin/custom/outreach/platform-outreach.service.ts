import { Injectable, inject } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse, Page } from '@core/crud/api.types';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
    CreateOutreachCampaignPayload,
    CreateOutreachContactPayload,
    CreateOutreachTemplatePayload,
    OutreachCampaignOutboxEntryVm,
    OutreachCampaignVm,
    OutreachContactVm,
    OutreachTemplateVm,
    UpdateOutreachContactPayload,
    UpdateOutreachTemplatePayload,
} from './platform-outreach.types';

@Injectable({ providedIn: 'root' })
export class PlatformOutreachService {
    readonly #api = inject(ApiClientService);

    getContacts(
        page: number,
        size: number,
        sort: string,
        search?: string
    ): Observable<Page<OutreachContactVm>> {
        const queryParams: Record<string, any> = { page, size, sort };
        if (search) {
            queryParams['search'] = search;
        }
        return this.#api
            .get<ApiResponse<Page<OutreachContactVm>>>(
                'platformOutreachContacts',
                undefined,
                queryParams
            )
            .pipe(map((r) => r.data));
    }

    getContactById(id: number): Observable<OutreachContactVm> {
        return this.#api
            .get<ApiResponse<OutreachContactVm>>('platformOutreachContactById', { id })
            .pipe(map((r) => r.data));
    }

    createContact(payload: CreateOutreachContactPayload): Observable<OutreachContactVm> {
        return this.#api
            .post<ApiResponse<OutreachContactVm>>('platformOutreachContacts', payload)
            .pipe(map((r) => r.data));
    }

    updateContact(id: number, payload: UpdateOutreachContactPayload): Observable<OutreachContactVm> {
        return this.#api
            .put<ApiResponse<OutreachContactVm>>('platformOutreachContactById', payload, { id })
            .pipe(map((r) => r.data));
    }

    deleteContact(id: number): Observable<void> {
        return this.#api
            .delete<ApiResponse<void>>('platformOutreachContactById', { id })
            .pipe(map(() => undefined));
    }

    getTemplates(): Observable<OutreachTemplateVm[]> {
        return this.#api
            .get<ApiResponse<OutreachTemplateVm[]>>('platformOutreachTemplates')
            .pipe(map((r) => r.data ?? []));
    }

    getTemplateById(id: number): Observable<OutreachTemplateVm> {
        return this.#api
            .get<ApiResponse<OutreachTemplateVm>>('platformOutreachTemplateById', { id })
            .pipe(map((r) => r.data));
    }

    createTemplate(payload: CreateOutreachTemplatePayload): Observable<OutreachTemplateVm> {
        return this.#api
            .post<ApiResponse<OutreachTemplateVm>>('platformOutreachTemplates', payload)
            .pipe(map((r) => r.data));
    }

    updateTemplate(id: number, payload: UpdateOutreachTemplatePayload): Observable<OutreachTemplateVm> {
        return this.#api
            .put<ApiResponse<OutreachTemplateVm>>('platformOutreachTemplateById', payload, { id })
            .pipe(map((r) => r.data));
    }

    deleteTemplate(id: number): Observable<void> {
        return this.#api
            .delete<ApiResponse<void>>('platformOutreachTemplateById', { id })
            .pipe(map(() => undefined));
    }

    getCampaigns(
        page: number,
        size: number,
        sort: string
    ): Observable<Page<OutreachCampaignVm>> {
        return this.#api
            .get<ApiResponse<Page<OutreachCampaignVm>>>(
                'platformOutreachCampaigns',
                undefined,
                { page, size, sort }
            )
            .pipe(map((r) => r.data));
    }

    getCampaignById(id: number): Observable<OutreachCampaignVm> {
        return this.#api
            .get<ApiResponse<OutreachCampaignVm>>('platformOutreachCampaignById', { id })
            .pipe(map((r) => r.data));
    }

    createCampaign(payload: CreateOutreachCampaignPayload): Observable<OutreachCampaignVm> {
        return this.#api
            .post<ApiResponse<OutreachCampaignVm>>('platformOutreachCampaigns', payload)
            .pipe(map((r) => r.data));
    }

    sendCampaign(id: number): Observable<OutreachCampaignVm> {
        return this.#api
            .post<ApiResponse<OutreachCampaignVm>>('platformOutreachCampaignSend', {}, { id })
            .pipe(map((r) => r.data));
    }

    getCampaignOutbox(id: number): Observable<OutreachCampaignOutboxEntryVm[]> {
        return this.#api
            .get<ApiResponse<OutreachCampaignOutboxEntryVm[]>>(
                'platformOutreachCampaignOutbox',
                { id }
            )
            .pipe(map((r) => r.data ?? []));
    }
}
