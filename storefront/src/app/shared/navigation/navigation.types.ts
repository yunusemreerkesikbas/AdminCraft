import { FuseNavigationItem } from '@fuse/components/navigation';

export interface AdminCraftNavigationItem extends FuseNavigationItem {
    requiredModule?: string | null;
    requiredRole?: string;
    excludedRoles?: string[];
    children?: AdminCraftNavigationItem[];
}

export interface NavigationConfig {
    items: AdminCraftNavigationItem[];
    language: string;
}
