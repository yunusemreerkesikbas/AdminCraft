import { inject, Injectable } from '@angular/core';
import { FuseNavigationItem } from '@fuse/components/navigation';
import { TranslocoService } from '@jsverse/transloco';
import { User } from 'app/core/user/user.types';
import { NavigationItem } from './navigation.types';

@Injectable({ providedIn: 'root' })
export class NavigationFilterService {
    #transloco = inject(TranslocoService);

    filterAndUpdateLinks(
        items: NavigationItem[],
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
        filtered = this.#removeEmptyGroups(filtered);
        filtered = this.#updateLinksWithLanguage(filtered, language);
        return filtered;
    }

    #filterByModulesAndRoles(
        items: NavigationItem[],
        userRole: string | undefined,
        enabledModules: string[]
    ): NavigationItem[] {
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
            .filter((item) => item !== null) as NavigationItem[];
    }

    #updateLinksWithLanguage(
        items: NavigationItem[],
        language: string
    ): NavigationItem[] {
        return items.map((item) => {
            const itemCopy = { ...item };
            if (
                itemCopy.link &&
                typeof itemCopy.link === 'string' &&
                !itemCopy.externalLink
            ) {
                itemCopy.link = `/${language}/${itemCopy.link}`;
            }
            if (itemCopy.title && typeof itemCopy.title === 'string') {
                const translated = this.#transloco.translate(
                    itemCopy.title,
                    {},
                    language
                );
                if (translated && translated !== itemCopy.title) {
                    itemCopy.title = translated;
                }
            }
            if (itemCopy.subtitle && typeof itemCopy.subtitle === 'string') {
                const translated = this.#transloco.translate(
                    itemCopy.subtitle,
                    {},
                    language
                );
                if (translated && translated !== itemCopy.subtitle) {
                    itemCopy.subtitle = translated;
                }
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

    #removeEmptyGroups(
        items: NavigationItem[]
    ): NavigationItem[] {
        return items.filter((item) => {
            if (item.children?.length) {
                item.children = this.#removeEmptyGroups(item.children);
                return item.children.length > 0;
            }
            if (Array.isArray(item.children) && item.children.length === 0) {
                return false;
            }
            return true;
        });
    }
}
