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
    entries: EntryDeliveryResponse[];
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

export interface PageDeliveryResponse {
    uid: string;
    title: string;
    subtitle: string;
    description: string;
    metaTitle: string;
    metaDescription: string;
    robotTag: string;
    urlPath: string;
    featuredImage: string;
    styleClasses: string;
    slots: Record<string, ComponentDeliveryResponse[]>;
}

export interface BatchPageDeliveryResponse {
    data: Record<string, PageDeliveryResponse>;
    meta: BatchMeta;
}
