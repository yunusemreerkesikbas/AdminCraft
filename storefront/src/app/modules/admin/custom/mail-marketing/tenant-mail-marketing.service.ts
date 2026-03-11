import { Injectable, inject } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse } from '@core/crud/api.types';
import { Observable, map } from 'rxjs';
import {
    MailCampaignVm,
    MailProviderConfigVm,
    MailSubscriberVm,
    MailTemplateTypeDetailVm,
    MailTemplateTypeSummaryVm,
    MailTemplateVm,
    UpdateMailTemplatePayload,
    UpsertMailProviderConfigPayload,
} from './mail-marketing.types';

@Injectable({ providedIn: 'root' })
export class TenantMailMarketingService {
    readonly #apiClient = inject(ApiClientService);

    getTemplateTypes(): Observable<MailTemplateTypeSummaryVm[]> {
        return this.#apiClient
            .get<ApiResponse<MailTemplateTypeSummaryVm[]>>('mailTemplateTypes')
            .pipe(map((response) => response.data ?? []));
    }

    getTemplateTypeDetail(
        templateType: string
    ): Observable<MailTemplateTypeDetailVm> {
        return this.#apiClient
            .get<
                ApiResponse<MailTemplateTypeDetailVm>
            >('mailTemplateTypeDetail', { templateType })
            .pipe(map((response) => response.data));
    }

    updateTemplateTranslation(
        templateType: string,
        language: string,
        payload: UpdateMailTemplatePayload
    ): Observable<ApiResponse<MailTemplateVm>> {
        return this.#apiClient.put<ApiResponse<MailTemplateVm>>(
            'mailTemplateTypeTranslation',
            payload,
            { templateType, language }
        );
    }

    getSubscribers(templateType: string): Observable<MailSubscriberVm[]> {
        return this.#apiClient
            .get<
                ApiResponse<MailSubscriberVm[]>
            >('mailSubscribers', undefined, { templateType })
            .pipe(map((response) => response.data ?? []));
    }

    getProviderConfig(): Observable<MailProviderConfigVm> {
        return this.#apiClient
            .get<ApiResponse<MailProviderConfigVm>>('mailProviderConfig')
            .pipe(map((response) => response.data));
    }

    upsertProviderConfig(
        payload: UpsertMailProviderConfigPayload
    ): Observable<ApiResponse<MailProviderConfigVm>> {
        return this.#apiClient.put<ApiResponse<MailProviderConfigVm>>(
            'mailProviderConfig',
            payload
        );
    }

    sendCampaign(templateId: number): Observable<ApiResponse<MailCampaignVm>> {
        return this.#apiClient.post<ApiResponse<MailCampaignVm>>(
            'mailCampaignSend',
            { templateId }
        );
    }

    getCampaign(id: number): Observable<MailCampaignVm> {
        return this.#apiClient
            .get<ApiResponse<MailCampaignVm>>('mailCampaignById', { id })
            .pipe(map((response) => response.data));
    }
}
