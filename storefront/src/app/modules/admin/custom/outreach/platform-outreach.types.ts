export type OutreachContactStatus = 'ACTIVE' | 'UNSUBSCRIBED';
export type OutreachCampaignStatus = 'DRAFT' | 'SENDING' | 'COMPLETED' | 'COMPLETED_WITH_ERRORS' | 'FAILED';
export type OutreachCampaignContactStatus = 'PENDING' | 'SENT' | 'FAILED';

export interface OutreachContactVm {
    id: number;
    uuid: string;
    fullName: string;
    email: string;
    companyName?: string | null;
    city?: string | null;
    notes?: string | null;
    status: OutreachContactStatus;
    createdAt: string;
}

export interface OutreachTemplateVm {
    id: number;
    uuid: string;
    name: string;
    subject: string;
    content: string;
    language: string;
    isActive: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface OutreachCampaignVm {
    id: number;
    uuid: string;
    name: string;
    subject: string;
    status: OutreachCampaignStatus;
    totalCount: number;
    sentCount: number;
    failedCount: number;
    createdByEmail?: string | null;
    createdAt: string;
    template?: OutreachTemplateVm | null;
}

export interface OutreachCampaignOutboxEntryVm {
    id: number;
    contactEmail: string;
    contactName?: string | null;
    renderedSubject?: string | null;
    status: OutreachCampaignContactStatus;
    providerMessageId?: string | null;
    errorMessage?: string | null;
    createdAt: string;
}

export interface CreateOutreachContactPayload {
    fullName: string;
    email: string;
    companyName?: string;
    city?: string;
    notes?: string;
}

export interface UpdateOutreachContactPayload extends CreateOutreachContactPayload {
    status: OutreachContactStatus;
}

export interface CreateOutreachTemplatePayload {
    name: string;
    subject: string;
    content: string;
    language: string;
}

export interface UpdateOutreachTemplatePayload extends CreateOutreachTemplatePayload {
    isActive: boolean;
}

export interface CreateOutreachCampaignPayload {
    name: string;
    templateId: number;
    contactIds: number[];
    subjectOverride?: string;
}
