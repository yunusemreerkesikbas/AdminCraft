import { Injectable, inject } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse } from '@core/crud/api.types';
import { Observable, map } from 'rxjs';
import {
    MailCampaignVm,
    MailSubscriberVm,
    MailTemplateTypeDetailVm,
    MailTemplateTypeSummaryVm,
    MailTemplateVm,
    UpdateMailTemplatePayload,
} from './mail-marketing.types';

@Injectable({ providedIn: 'root' })
export class PlatformMailMarketingService {
    readonly #apiClient = inject(ApiClientService);

    getTemplateTypes(): Observable<MailTemplateTypeSummaryVm[]> {
        return this.#apiClient
            .get<
                ApiResponse<MailTemplateTypeSummaryVm[]>
            >('platformMailTemplateTypes')
            .pipe(map((response) => response.data ?? []));
    }

    getTemplateTypeDetail(
        templateType: string
    ): Observable<MailTemplateTypeDetailVm> {
        return this.#apiClient
            .get<
                ApiResponse<MailTemplateTypeDetailVm>
            >('platformMailTemplateTypeDetail', { templateType })
            .pipe(map((response) => response.data));
    }

    updateTemplateTranslation(
        templateType: string,
        language: string,
        payload: UpdateMailTemplatePayload
    ): Observable<ApiResponse<MailTemplateVm>> {
        return this.#apiClient.put<ApiResponse<MailTemplateVm>>(
            'platformMailTemplateTypeTranslation',
            payload,
            { templateType, language }
        );
    }

    getSubscribers(templateType: string): Observable<MailSubscriberVm[]> {
        return this.#apiClient
            .get<
                ApiResponse<MailSubscriberVm[]>
            >('platformMailSubscribers', undefined, { templateType })
            .pipe(map((response) => response.data ?? []));
    }

    sendCampaign(templateId: number): Observable<ApiResponse<MailCampaignVm>> {
        return this.#apiClient.post<ApiResponse<MailCampaignVm>>(
            'platformMailCampaignSend',
            {
                templateId,
            }
        );
    }

    getCampaign(id: number): Observable<MailCampaignVm> {
        return this.#apiClient
            .get<
                ApiResponse<MailCampaignVm>
            >('platformMailCampaignById', { id })
            .pipe(map((response) => response.data));
    }
}
