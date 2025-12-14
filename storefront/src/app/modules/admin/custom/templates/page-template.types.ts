export type SlotPosition = 'TOP' | 'CENTER' | 'BOTTOM' | 'LEFT' | 'RIGHT';

export interface PageTemplate {
    id: number;
    uuid: string;
    uid: string;
    name: string;
    description?: string;
    isSystem: boolean;
    isActive: boolean;
    slots: TemplateSlot[];
}

export interface TemplateSlot {
    id: number;
    uuid: string;
    slotName: string;
    position: SlotPosition;
    sortOrder: number;
    isRequired: boolean;
    maxComponents?: number;
    allowedTypes?: string[];
}

export interface CreatePageTemplateDto {
    name: string;
    uid: string;
    description?: string;
    isActive?: boolean;
}

export interface UpdatePageTemplateDto {
    name?: string;
    description?: string;
    isActive?: boolean;
}

export interface CreateTemplateSlotDto {
    slotName: string;
    position: SlotPosition;
    sortOrder?: number;
    isRequired?: boolean;
    maxComponents?: number;
    allowedTypes?: string[];
}

export interface PageTemplateListItem {
    id: number;
    uid: string;
    name: string;
    description?: string;
    isSystem: boolean;
    isActive: boolean;
    slotsCount: number;
}

export const SLOT_POSITIONS: readonly { value: SlotPosition; label: string }[] = [
    { value: 'TOP', label: 'Top' },
    { value: 'CENTER', label: 'Center' },
    { value: 'BOTTOM', label: 'Bottom' },
    { value: 'LEFT', label: 'Left' },
    { value: 'RIGHT', label: 'Right' }
] as const;
