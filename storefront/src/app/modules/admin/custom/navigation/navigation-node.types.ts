import { NodePosition } from '@shared/types/common.types';
export { NodePosition };

// Matches backend: com.backend.domain.enums.NavigationItemType
export enum NavigationItemType {
    URL = 'URL',
    PAGE = 'PAGE',
    COMPONENT = 'COMPONENT'
}

export const NAVIGATION_ITEM_TYPE_OPTIONS = [
    { value: NavigationItemType.URL, label: 'URL' },
    { value: NavigationItemType.PAGE, label: 'Page' },
    { value: NavigationItemType.COMPONENT, label: 'Component' }
];

export interface NavigationNode {
    id: number;
    uid: string;
    title: string;
    position: NodePosition;
    isVisible: boolean;
    isTab: boolean;
    children?: NavigationNode[];
    entries?: NavigationEntry[];
}

// Matches backend: NavigationEntryResponse
export interface NavigationEntry {
    id: number;
    uid: string;
    nodeId: number;
    itemType: NavigationItemType;
    itemId?: string;
    url?: string;
    linkName: string;
    linkColor?: string;
    target: string;
    isExternal: boolean;
    isVisible: boolean;
    sortOrder: number;
}

export interface CreateNodeRequest {
    uid: string;
    title: string;
    position: NodePosition;
    isVisible: boolean;
    isTab: boolean;
    parentId?: number | null;
}

export interface UpdateNodeRequest {
    title: string;
    position: NodePosition;
    isVisible: boolean;
    isTab: boolean;
}

// Matches backend: CreateEntryRequest
export interface CreateEntryRequest {
    nodeId: number;
    uid: string;
    itemType: NavigationItemType;
    itemId?: string;
    url?: string;
    linkName: string;
    linkColor?: string;
    target?: string;
    isExternal?: boolean;
    isVisible?: boolean;
}

// Matches backend: UpdateEntryRequest
export interface UpdateEntryRequest {
    itemType?: NavigationItemType;
    itemId?: string;
    url?: string;
    linkName?: string;
    linkColor?: string;
    target?: string;
    isExternal?: boolean;
    isVisible?: boolean;
}

export interface ReorderRequest {
    items: number[];
}

