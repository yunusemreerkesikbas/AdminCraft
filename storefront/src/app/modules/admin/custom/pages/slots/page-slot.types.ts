import { ComponentDeliveryResponse } from 'app/cms/cms-delivery.types';

export interface PageSlotResponse {
    id: number;
    uuid: string;
    uid: string;
    pageId: number | null; // null for shared slots
    slotName: string;
    position: string;
    sortOrder: number;
    isActive: boolean;
    isShared: boolean;
    components: SlotComponentResponse[];
}

export interface SlotComponentResponse {
    id: number;
    slotId: number;
    componentId: number;
    sortOrder: number;
    isVisible: boolean;
    component?: ComponentDeliveryResponse; // Optional expanded data
}

export interface CreatePageSlotRequest {
    uid?: string;
    slotName: string;
    position: string;
    sortOrder?: number;
    isActive?: boolean;
    isShared?: boolean;
}

export interface UpdatePageSlotRequest {
    uid?: string;
    slotName: string;
    position: string;
    sortOrder?: number;
    isActive?: boolean;
    isShared?: boolean;
}

export interface AddComponentToSlotRequest {
    componentId: number;
    sortOrder?: number;
}

export interface ReorderSlotComponentsRequest {
    componentIds: number[];
}
