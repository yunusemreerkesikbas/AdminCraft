import { Injectable } from '@angular/core';
import { FuseNavigationItem } from '@fuse/components/navigation';
import { User } from 'app/core/user/user.types';
import { AdminCraftNavigationItem } from './navigation.types';

@Injectable({ providedIn: 'root' })
export class NavigationFilterService {

    filterAndUpdateLinks(
        items: AdminCraftNavigationItem[],
        user: User | null,
        enabledModules: string[],
        language: string
    ): FuseNavigationItem[] {
        const userRole = user?.role;
        let filtered = this.#filterByModulesAndRoles(
            items,
            userRole,
            enabledModules
        );
        filtered = this.#updateLinksWithLanguage(filtered, language);
        return filtered;
    }

    #filterByModulesAndRoles(
        items: AdminCraftNavigationItem[],
        userRole: string | undefined,
        enabledModules: string[]
    ): AdminCraftNavigationItem[] {
        return items
            .map((item) => {
                const itemCopy = { ...item };
                if (
                    itemCopy.requiredRole &&
                    userRole !== itemCopy.requiredRole
                ) {
                    return null;
                }
                if (itemCopy.excludedRoles?.includes(userRole)) {
                    return null;
                }
                if (!itemCopy.requiredModule) {
                    if (itemCopy.children?.length) {
                        itemCopy.children = this.#filterByModulesAndRoles(
                            itemCopy.children,
                            userRole,
                            enabledModules
                        );
                    }
                    return itemCopy;
                }
                if (enabledModules.includes(itemCopy.requiredModule)) {
                    if (itemCopy.children?.length) {
                        itemCopy.children = this.#filterByModulesAndRoles(
                            itemCopy.children,
                            userRole,
                            enabledModules
                        );
                    }
                    return itemCopy;
                }
                return null;
            })
            .filter((item) => item !== null) as AdminCraftNavigationItem[];
    }

    #updateLinksWithLanguage(
        items: AdminCraftNavigationItem[],
        language: string
    ): AdminCraftNavigationItem[] {
        return items.map((item) => {
            const itemCopy = { ...item };
            if (
                itemCopy.link &&
                typeof itemCopy.link === 'string' &&
                !itemCopy.externalLink
            ) {
                itemCopy.link = `/${language}/${itemCopy.link}`;
            }
            if (itemCopy.children?.length) {
                itemCopy.children = this.#updateLinksWithLanguage(
                    itemCopy.children,
                    language
                );
            }
            return itemCopy;
        });
    }
}
