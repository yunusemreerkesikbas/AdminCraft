import { Injectable } from '@angular/core';
import { ApiResponse } from '@core/crud/api.types';
import { Observable, map } from 'rxjs';
import { BaseMailMarketingService, MailMarketingEndpointConfig } from './base-mail-marketing.service';
import {
    MailProviderConfigVm,
    UpsertMailProviderConfigPayload,
} from './mail-marketing.types';

@Injectable({ providedIn: 'root' })
export class TenantMailMarketingService extends BaseMailMarketingService {
    protected override readonly endpointConfig: MailMarketingEndpointConfig = {
        templateTypes: 'mailTemplateTypes',
        templateTypeDetail: 'mailTemplateTypeDetail',
        templateTypeTranslation: 'mailTemplateTypeTranslation',
        subscribers: 'mailSubscribers',
        campaignSend: 'mailCampaignSend',
        campaignList: 'mailCampaignList',
        campaignById: 'mailCampaignById',
        campaignOutbox: 'mailCampaignOutbox',
    };

    getProviderConfig(): Observable<MailProviderConfigVm> {
        return this.apiClient
            .get<ApiResponse<MailProviderConfigVm>>('mailProviderConfig')
            .pipe(map((r) => r.data));
    }

    upsertProviderConfig(
        payload: UpsertMailProviderConfigPayload
    ): Observable<ApiResponse<MailProviderConfigVm>> {
        return this.apiClient.put<ApiResponse<MailProviderConfigVm>>(
            'mailProviderConfig',
            payload
        );
    }
}
