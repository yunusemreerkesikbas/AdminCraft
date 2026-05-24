export type SmartEditMessageType =
    | 'smartedit:ready'
    | 'smartedit:select'
    | 'smartedit:reload';

export interface SmartEditReadyMessage {
    type: 'smartedit:ready';
    payload: {
        href: string;
        lang: string | null;
    };
}

export interface SmartEditRectInfo {
    x: number;
    y: number;
    width: number;
    height: number;
}

export interface SmartEditComponentSelection {
    kind: 'component';
    id: string;
    componentType: string | null;
    rect: SmartEditRectInfo;
}

export interface SmartEditSlotSelection {
    kind: 'slot';
    id: string;
    slotName: string | null;
    position: string | null;
    shared: boolean;
    rect: SmartEditRectInfo;
}

export type SmartEditSelection = SmartEditComponentSelection | SmartEditSlotSelection;

export interface SmartEditSelectMessage {
    type: 'smartedit:select';
    payload: SmartEditSelection;
}

export interface SmartEditReloadMessage {
    type: 'smartedit:reload';
    payload?: undefined;
}

export type SmartEditInboundMessage = SmartEditReadyMessage | SmartEditSelectMessage;
export type SmartEditOutboundMessage = SmartEditReloadMessage;

export interface PreviewTicketResponse {
    ticket: string;
    expiresAt: string;
    storefrontBaseUrl: string;
}

export interface PreviewTicketIssueRequest {
    pageId?: number;
}

export type SmartEditDraftTargetType =
    | 'COMPONENT'
    | 'COMPONENT_I18N'
    | 'COMPONENT_ENTRY'
    | 'COMPONENT_ENTRY_I18N';

export interface SmartEditDraftFieldChange {
    field: string;
    label: string;
    before: unknown;
    after: unknown;
    valueType: string;
}

export interface SmartEditDraftItem {
    draftId: number;
    targetType: SmartEditDraftTargetType;
    targetId: number;
    language?: string | null;
    componentId?: number | null;
    componentUid?: string | null;
    componentName?: string | null;
    entryId?: number | null;
    entryUid?: string | null;
    fieldChanges: SmartEditDraftFieldChange[];
    updatedAt?: string | null;
    updatedBy?: number | null;
}

export interface SmartEditDraftOverview {
    count: number;
    drafts: SmartEditDraftItem[];
}
