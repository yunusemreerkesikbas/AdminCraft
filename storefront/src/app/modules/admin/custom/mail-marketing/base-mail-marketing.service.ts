import { inject } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse } from '@core/crud/api.types';
import { EndpointKey } from '@modules/admin/api-endpoints';
import { Observable, map } from 'rxjs';
import {
    MailCampaignVm,
    MailOutboxEntryVm,
    MailSubscriberVm,
    MailTemplateTypeDetailVm,
    MailTemplateTypeSummaryVm,
    MailTemplateVm,
    UpdateMailTemplatePayload,
} from './mail-marketing.types';

export interface MailMarketingEndpointConfig {
    templateTypes: EndpointKey;
    templateTypeDetail: EndpointKey;
    templateTypeTranslation: EndpointKey;
    subscribers: EndpointKey;
    campaignSend: EndpointKey;
    campaignList: EndpointKey;
    campaignById: EndpointKey;
    campaignOutbox: EndpointKey;
}

export abstract class BaseMailMarketingService {
    protected readonly apiClient = inject(ApiClientService);
    protected abstract readonly endpointConfig: MailMarketingEndpointConfig;

    getTemplateTypes(): Observable<MailTemplateTypeSummaryVm[]> {
        return this.apiClient
            .get<ApiResponse<MailTemplateTypeSummaryVm[]>>(this.endpointConfig.templateTypes)
            .pipe(map((r) => r.data ?? []));
    }

    getTemplateTypeDetail(templateType: string): Observable<MailTemplateTypeDetailVm> {
        return this.apiClient
            .get<ApiResponse<MailTemplateTypeDetailVm>>(this.endpointConfig.templateTypeDetail, { templateType })
            .pipe(map((r) => r.data));
    }

    updateTemplateTranslation(
        templateType: string,
        language: string,
        payload: UpdateMailTemplatePayload
    ): Observable<ApiResponse<MailTemplateVm>> {
        return this.apiClient.put<ApiResponse<MailTemplateVm>>(
            this.endpointConfig.templateTypeTranslation,
            payload,
            { templateType, language }
        );
    }

    getSubscribers(templateType: string): Observable<MailSubscriberVm[]> {
        return this.apiClient
            .get<ApiResponse<MailSubscriberVm[]>>(this.endpointConfig.subscribers, undefined, { templateType })
            .pipe(map((r) => r.data ?? []));
    }

    sendCampaign(templateType: string): Observable<ApiResponse<MailCampaignVm>> {
        return this.apiClient.post<ApiResponse<MailCampaignVm>>(
            this.endpointConfig.campaignSend,
            { templateType }
        );
    }

    getCampaignList(templateType: string, size = 5): Observable<MailCampaignVm[]> {
        return this.apiClient
            .get<ApiResponse<MailCampaignVm[]>>(this.endpointConfig.campaignList, undefined, { templateType, size })
            .pipe(map((r) => r.data ?? []));
    }

    getCampaign(id: number): Observable<MailCampaignVm> {
        return this.apiClient
            .get<ApiResponse<MailCampaignVm>>(this.endpointConfig.campaignById, { id })
            .pipe(map((r) => r.data));
    }

    getCampaignOutbox(id: number): Observable<MailOutboxEntryVm[]> {
        return this.apiClient
            .get<ApiResponse<MailOutboxEntryVm[]>>(this.endpointConfig.campaignOutbox, { id })
            .pipe(map((r) => r.data ?? []));
    }
}
