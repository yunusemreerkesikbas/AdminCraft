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
