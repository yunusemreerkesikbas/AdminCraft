export type MailCampaignStatus =
    | 'DRAFT'
    | 'SENDING'
    | 'COMPLETED'
    | 'COMPLETED_WITH_ERRORS'
    | 'FAILED';

export type MailSubscriberStatus =
    | 'PENDING_CONFIRMATION'
    | 'ACTIVE'
    | 'UNSUBSCRIBED';

export interface MailTemplateVm {
    id: number;
    templateKey: string;
    language: string;
    subject: string;
    content: string;
    active: boolean;
}

export interface MailTemplateTypeSummaryVm {
    templateType: string;
    active: boolean;
    languages: string[];
    subscriberCount: number;
    lastCampaignAt?: string | null;
}

export interface MailTemplateTypeDetailVm {
    templateType: string;
    templates: MailTemplateVm[];
    lastCampaign?: MailCampaignVm | null;
    subscriberCount: number;
}

export interface UpdateMailTemplatePayload {
    subject: string;
    content: string;
    active: boolean;
}

export interface MailProviderConfigVm {
    provider: string;
    fromEmail: string;
    fromName: string;
    active: boolean;
    tokenConfigured: boolean;
}

export interface UpsertMailProviderConfigPayload {
    fromEmail: string;
    fromName: string;
    serverToken?: string | null;
    active: boolean;
}

export interface MailCampaignVm {
    id: number;
    status: MailCampaignStatus;
    totalCount: number;
    sentCount: number;
    failedCount: number;
    sentPercent?: number | null;
    createdAt?: string | null;
}

export type MailOutboxStatus = 'PROCESSING' | 'SENT' | 'FAILED';

export interface MailOutboxEntryVm {
    id: number;
    toEmail: string;
    status: MailOutboxStatus;
    errorMessage?: string | null;
    providerMessageId?: string | null;
}

export interface MailSubscriberVm {
    id: number;
    email: string;
    status: MailSubscriberStatus;
    source?: string | null;
    preferredLanguage?: string | null;
    permission?: boolean | null;
    createdAt: string;
    confirmedAt?: string | null;
    unsubscribedAt?: string | null;
}

export interface MailSubscriberSubscriptionVm {
    templateType: string;
    source?: string | null;
    preferredLanguage?: string | null;
    permission: boolean;
}

export interface MailSubscriberAdminVm {
    id: number;
    email: string;
    status: MailSubscriberStatus;
    subscriptions: MailSubscriberSubscriptionVm[];
    createdAt: string;
    confirmedAt?: string | null;
    unsubscribedAt?: string | null;
}

export interface UpsertMailSubscriberPayload {
    email: string;
    status: MailSubscriberStatus;
    subscriptions: MailSubscriberSubscriptionVm[];
}
