import { Injectable } from '@angular/core';
import { BaseMailMarketingService, MailMarketingEndpointConfig } from './base-mail-marketing.service';

@Injectable({ providedIn: 'root' })
export class PlatformMailMarketingService extends BaseMailMarketingService {
    protected override readonly endpointConfig: MailMarketingEndpointConfig = {
        templateTypes: 'platformMailTemplateTypes',
        templateTypeDetail: 'platformMailTemplateTypeDetail',
        templateTypeTranslation: 'platformMailTemplateTypeTranslation',
        subscribers: 'platformMailSubscribers',
        campaignSend: 'platformMailCampaignSend',
        campaignList: 'platformMailCampaignList',
        campaignById: 'platformMailCampaignById',
        campaignOutbox: 'platformMailCampaignOutbox',
    };
}
