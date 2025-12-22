import { Language, NodePosition } from '@shared/types/common.types';
export { NodePosition };
export type { Language };

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

export interface NavigationNodeI18n {
    id?: number;
    nodeId: number;
    language: string;
    title: string;
}

export interface NavigationEntryI18n {
    id?: number;
    entryId: number;
    language: string;
    linkName: string;
}

export interface NodeI18nRequest {
    title: string;
}

export interface EntryI18nRequest {
    linkName: string;
}

export interface LanguageTab {
    code: string;
    label: string;
}

export interface CreateNodeCompositeRequest {
    uid: string;
    position: NodePosition;
    isVisible: boolean;
    isTab: boolean;
    parentId?: number | null;
    translations: Record<Language, NodeI18nRequest>;
}

export interface UpdateNodeCompositeRequest {
    position?: NodePosition;
    isVisible?: boolean;
    isTab?: boolean;
    translations: Record<Language, NodeI18nRequest>;
}

export interface NodeCompositeResponse {
    id: number;
    uuid: string;
    uid: string;
    parentId?: number | null;
    position: NodePosition;
    sortOrder: number;
    isVisible: boolean;
    isTab: boolean;
    translations: Record<Language, NavigationNodeI18n>;
}

export interface CreateEntryCompositeRequest {
    nodeId: number;
    uid: string;
    itemType: NavigationItemType;
    itemId?: string;
    url?: string;
    linkColor?: string;
    target?: string;
    isExternal?: boolean;
    isVisible?: boolean;
    translations: Record<Language, EntryI18nRequest>;
}

export interface UpdateEntryCompositeRequest {
    itemType?: NavigationItemType;
    itemId?: string;
    url?: string;
    linkColor?: string;
    target?: string;
    isExternal?: boolean;
    isVisible?: boolean;
    translations: Record<Language, EntryI18nRequest>;
}

export interface EntryCompositeResponse {
    id: number;
    uuid: string;
    uid: string;
    nodeId: number;
    itemType: NavigationItemType;
    itemId?: string;
    url?: string;
    linkColor?: string;
    target: string;
    isExternal: boolean;
    sortOrder: number;
    isVisible: boolean;
    translations: Record<Language, NavigationEntryI18n>;
}



