import { Injectable, OnDestroy } from '@angular/core';
import { FuseNavigationItem } from '@fuse/components/navigation';
import { FuseMockApiService } from '@fuse/lib/mock-api';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import {
    defaultNavigation,
} from 'app/mock-api/common/navigation/data';
import { cloneDeep } from 'lodash-es';
import { Subject, takeUntil } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NavigationMockApi implements OnDestroy {
    
    private readonly _defaultNavigation: FuseNavigationItem[] =
        defaultNavigation;
    private _enabledModules: string[] = [];
    #destroy$ = new Subject<void>();


    constructor(
        private _fuseMockApiService: FuseMockApiService,
        private _tenantContext: TenantContextService
    ) {
        this._tenantContext.tenantModules$
            .pipe(takeUntil(this.#destroy$))
            .subscribe((modules) => {
                this._enabledModules = modules;
            });

        this.registerHandlers();
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    registerHandlers(): void {
        // -----------------------------------------------------------------------------------------------------
        // @ Navigation - GET
        // -----------------------------------------------------------------------------------------------------
        this._fuseMockApiService.onGet('api/common/navigation').reply(() => {
           

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
            };

            updateLinks(navCopy.default);

            // Filter navigation by enabled modules
            const filteredNav = {
                default: this.filterNavigationByModules(navCopy.default),
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
