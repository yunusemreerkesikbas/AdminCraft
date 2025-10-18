import { Injectable } from '@angular/core';
import { FuseNavigationItem } from '@fuse/components/navigation';
import { FuseMockApiService } from '@fuse/lib/mock-api';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import {
    compactNavigation,
    defaultNavigation,
    futuristicNavigation,
    horizontalNavigation,
} from 'app/mock-api/common/navigation/data';
import { cloneDeep } from 'lodash-es';

@Injectable({ providedIn: 'root' })
export class NavigationMockApi {
    private readonly _compactNavigation: FuseNavigationItem[] =
        compactNavigation;
    private readonly _defaultNavigation: FuseNavigationItem[] =
        defaultNavigation;
    private readonly _futuristicNavigation: FuseNavigationItem[] =
        futuristicNavigation;
    private readonly _horizontalNavigation: FuseNavigationItem[] =
        horizontalNavigation;

    private _enabledModules: string[] = [];


    constructor(
        private _fuseMockApiService: FuseMockApiService,
        private _tenantContext: TenantContextService
    ) {
        this._tenantContext.tenantModules$.subscribe((modules) => {
            this._enabledModules = modules;
        });

        this.registerHandlers();
    }

    registerHandlers(): void {
        // -----------------------------------------------------------------------------------------------------
        // @ Navigation - GET
        // -----------------------------------------------------------------------------------------------------
        this._fuseMockApiService.onGet('api/common/navigation').reply(() => {
            // Fill compact navigation children using the default navigation
            this._compactNavigation.forEach((compactNavItem) => {
                this._defaultNavigation.forEach((defaultNavItem) => {
                    if (defaultNavItem.id === compactNavItem.id) {
                        compactNavItem.children = cloneDeep(
                            defaultNavItem.children
                        );
                    }
                });
            });

            // Fill futuristic navigation children using the default navigation
            this._futuristicNavigation.forEach((futuristicNavItem) => {
                this._defaultNavigation.forEach((defaultNavItem) => {
                    if (defaultNavItem.id === futuristicNavItem.id) {
                        futuristicNavItem.children = cloneDeep(
                            defaultNavItem.children
                        );
                    }
                });
            });

            // Fill horizontal navigation children using the default navigation
            this._horizontalNavigation.forEach((horizontalNavItem) => {
                this._defaultNavigation.forEach((defaultNavItem) => {
                    if (defaultNavItem.id === horizontalNavItem.id) {
                        horizontalNavItem.children = cloneDeep(
                            defaultNavItem.children
                        );
                    }
                });
            });

            const currentLang = localStorage.getItem('lang') || 'tr';
            const currentTenant = localStorage.getItem('currentTenantSubdomain') || 'default';

            const updateLinks = (items: any[]) => {
                items?.forEach((item) => {
                    if (item.link && typeof item.link === 'string') {
                        if (item.link.includes('dashboards/')) {
                            item.link = `/${currentLang}/${item.link}`;
                        } else {
                            item.link = `/${currentLang}/${currentTenant}/${item.link}`;
                        }
                    }
                    if (item.children?.length) {
                        updateLinks(item.children);
                    }
                });
            };

            const navCopy = {
                default: cloneDeep(this._defaultNavigation),
                compact: cloneDeep(this._compactNavigation),
                futuristic: cloneDeep(this._futuristicNavigation),
                horizontal: cloneDeep(this._horizontalNavigation)
            };

            updateLinks(navCopy.default);
            updateLinks(navCopy.compact);
            updateLinks(navCopy.futuristic);
            updateLinks(navCopy.horizontal);

            // Filter navigation by enabled modules
            const filteredNav = {
                default: this.filterNavigationByModules(navCopy.default),
                compact: this.filterNavigationByModules(navCopy.compact),
                futuristic: this.filterNavigationByModules(navCopy.futuristic),
                horizontal: this.filterNavigationByModules(navCopy.horizontal)
            };

            return [
                200,
                filteredNav,
            ];
        });
    }

    /**
     * Filter navigation items based on enabled modules
     */
    private filterNavigationByModules(items: FuseNavigationItem[]): FuseNavigationItem[] {
        return items
            .map(item => {
                const itemCopy = { ...item };

                // Platform module (requiredModule === null or undefined) → always show
                if (!itemCopy.requiredModule) {
                    if (itemCopy.children?.length) {
                        itemCopy.children = this.filterNavigationByModules(itemCopy.children);
                    }
                    return itemCopy;
                }

                // Tenant module → show only if enabled
                if (this._enabledModules.includes(itemCopy.requiredModule)) {
                    if (itemCopy.children?.length) {
                        itemCopy.children = this.filterNavigationByModules(itemCopy.children);
                    }
                    return itemCopy;
                }

                // Module not enabled → hide item
                return null;
            })
            .filter(item => item !== null) as FuseNavigationItem[];
    }
}
