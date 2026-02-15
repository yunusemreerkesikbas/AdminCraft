export interface EntryDeliveryResponse {
    uid: string;
    order: number;
    title: string;
    description?: string;
    isVisible: boolean;
    styleClasses?: string;
    customFields: Record<string, unknown>;
}

export interface ComponentDeliveryResponse {
    uid: string;
    type: string;
    category: string;
    title: string;
    subtitle?: string;
    description?: string;
    isVisible: boolean;
    styleClasses?: string;
    navigationType?: string;
    searchBox?: boolean;
    navigationNode?: NavigationDeliveryResponse | null;
    navigationLinkNode?: NavigationDeliveryResponse | null;
    entries: EntryDeliveryResponse[];
}

export interface NavigationEntryDeliveryResponse {
    uid: string;
    itemType: string;
    itemId?: string;
    url?: string;
    linkName?: string;
    linkColor?: string;
    target?: string;
    isExternal?: boolean;
}

export interface NavigationDeliveryResponse {
    uid: string;
    title?: string;
    position?: string;
    isTab?: boolean;
    entries?: NavigationEntryDeliveryResponse[];
    children?: NavigationDeliveryResponse[];
}

export interface BatchMeta {
    requested: number;
    found: number;
    notFound: string[];
}

export interface BatchDeliveryResponse {
    data: Record<string, ComponentDeliveryResponse>;
    meta: BatchMeta;
}

export interface ContentSlotDeliveryResponse {
    slotId: string;
    slotUuid: string;
    position: string;
    name: string;
    slotShared: boolean;
    components: { componentList: ComponentDeliveryResponse[] };
}

export interface ContentSlotsWrapper {
    contentSlot: ContentSlotDeliveryResponse[];
}

export interface PageDeliveryResponse {
    uid: string;
    name: string;
    title: string;
    description: string;
    robotTag: string;
    canonicalUrl: string;
    styleClasses: string;
    template: string;
    typeCode: string;
    contentSlots: ContentSlotsWrapper;
    slots: Record<string, ComponentDeliveryResponse[]>;
}

export interface LanguageInfo {
    code: string;
    nativeName: string;
    isRtl: boolean;
}

export interface SiteDeliveryResponse {
    siteName: string;
    siteTitle: string;
    siteDescription: string;
    siteKeywords: string;
    ogImageUrl: string;
    defaultLanguage: string;
    enabledLanguages: LanguageInfo[];
    themeName: string;
    maintenanceMode: boolean;
    maintenanceMessage: string;
    googleAnalyticsId: string;
    googleTagManagerId: string;
    twitterHandle: string;
    facebookPageUrl: string;
    domain: string;
    customDomain: string;
    sslEnabled: boolean;
}
