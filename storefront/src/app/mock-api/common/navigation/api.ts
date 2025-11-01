import { Injectable, effect, inject } from '@angular/core';
import { FuseNavigationItem } from '@fuse/components/navigation';
import { FuseMockApiService } from '@fuse/lib/mock-api';
import { UserService } from 'app/core/user/user.service';
import {
    defaultNavigation,
} from 'app/mock-api/common/navigation/data';
import { cloneDeep } from 'lodash-es';

@Injectable({ providedIn: 'root' })
export class NavigationMockApi {
    private readonly _userService = inject(UserService);

    private readonly _defaultNavigation: FuseNavigationItem[] =
        defaultNavigation;
    private _enabledModules: string[] = [];

    constructor(
        private _fuseMockApiService: FuseMockApiService
    ) {
        effect(() => {
            this._enabledModules = this._userService.tenantModules();
        });

        this.registerHandlers();
    }

    registerHandlers(): void {
        this._fuseMockApiService.onGet('api/common/navigation').reply(() => {
           

            const currentLang = (navigator.language?.startsWith('en') ? 'en' : 'tr');

            const updateLinks = (items: any[]) => {
                items?.forEach((item) => {
                    if (item.link && typeof item.link === 'string') {
                        item.link = `/${currentLang}/${item.link}`;
                    }
                    if (item.children?.length) {
                        updateLinks(item.children);
                    }
                });
            };

            const navCopy = {
                default: cloneDeep(this._defaultNavigation),
            };

            updateLinks(navCopy.default);
            const filteredNav = {
                default: this.filterNavigationByModulesAndRoles(navCopy.default),
            };

            return [
                200,
                filteredNav,
            ];
        });
    }

    private filterNavigationByModulesAndRoles(items: FuseNavigationItem[]): FuseNavigationItem[] {
        const user = this._userService.user();
        const userRole = user?.role;

        return items
            .map(item => {
                const itemCopy = { ...item };

                // 1. Check role-based restrictions FIRST
                if (itemCopy.requiredRole && userRole !== itemCopy.requiredRole) {
                    return null; // Hide if required role doesn't match
                }
                if (itemCopy.excludedRoles?.includes(userRole)) {
                    return null; // Hide if user role is excluded
                }

                // 2. Then check module restrictions
                if (!itemCopy.requiredModule) {
                    // Platform module - no module restriction
                    if (itemCopy.children?.length) {
                        itemCopy.children = this.filterNavigationByModulesAndRoles(itemCopy.children);
                    }
                    return itemCopy;
                }

                // Check if user has the required module
                if (this._enabledModules.includes(itemCopy.requiredModule)) {
                    if (itemCopy.children?.length) {
                        itemCopy.children = this.filterNavigationByModulesAndRoles(itemCopy.children);
                    }
                    return itemCopy;
                }

                return null; // Module not enabled
            })
            .filter(item => item !== null) as FuseNavigationItem[];
    }
}
