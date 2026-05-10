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
