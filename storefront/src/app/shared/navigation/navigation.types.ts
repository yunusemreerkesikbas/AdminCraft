import { FuseNavigationItem } from '@fuse/components/navigation';

export interface NavigationItem extends FuseNavigationItem {
    requiredModule?: string | null;
    requiredRole?: string;
    excludedRoles?: string[];
    children?: NavigationItem[];
}

export interface NavigationConfig {
    items: NavigationItem[];
    language: string;
}
