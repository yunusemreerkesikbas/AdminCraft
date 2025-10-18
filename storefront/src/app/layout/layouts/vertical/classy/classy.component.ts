import { Component, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute, Router, RouterOutlet } from '@angular/router';
import { FuseFullscreenComponent } from '@fuse/components/fullscreen';
import { FuseLoadingBarComponent } from '@fuse/components/loading-bar';
import {
    FuseNavigationService,
    FuseVerticalNavigationComponent,
} from '@fuse/components/navigation';
import { FuseMediaWatcherService } from '@fuse/services/media-watcher';
import { NavigationService } from 'app/core/navigation/navigation.service';
import { Navigation } from 'app/core/navigation/navigation.types';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { UserService } from 'app/core/user/user.service';
import { User } from 'app/core/user/user.types';
import { LanguagesComponent } from 'app/layout/common/languages/languages.component';
import { MessagesComponent } from 'app/layout/common/messages/messages.component';
import { NotificationsComponent } from 'app/layout/common/notifications/notifications.component';
import { SearchComponent } from 'app/layout/common/search/search.component';
import { ShortcutsComponent } from 'app/layout/common/shortcuts/shortcuts.component';
import { UserComponent } from 'app/layout/common/user/user.component';
import { TenantsService } from 'app/modules/admin/custom/tenants/tenants.service';
import { Tenant } from 'app/modules/admin/custom/tenants/tenants.types';
import { SpaSelectComponent, SpaSelectOption } from 'app/shared/components/custom-ui/spa-select/spa-select.component';
import { Subject, takeUntil } from 'rxjs';

@Component({
    selector: 'classy-layout',
    templateUrl: './classy.component.html',
    encapsulation: ViewEncapsulation.None,
    imports: [
        FuseLoadingBarComponent,
        FuseVerticalNavigationComponent,
        NotificationsComponent,
        UserComponent,
        MatIconModule,
        MatButtonModule,
        LanguagesComponent,
        FuseFullscreenComponent,
        SearchComponent,
        ShortcutsComponent,
        MessagesComponent,
        RouterOutlet,
        SpaSelectComponent,
        FormsModule,
    ],
})
export class ClassyLayoutComponent implements OnInit, OnDestroy {
    isScreenSmall: boolean;
    navigation: Navigation;
    user: User;
    isSuperAdmin: boolean = false;
    tenantOptions: SpaSelectOption<number>[] = [];
    selectedTenantId: number | null = null;
    private tenants: Tenant[] = [];
    private _unsubscribeAll: Subject<any> = new Subject<any>();

    constructor(
        private _activatedRoute: ActivatedRoute,
        private _router: Router,
        private _navigationService: NavigationService,
        private _userService: UserService,
        private _fuseMediaWatcherService: FuseMediaWatcherService,
        private _fuseNavigationService: FuseNavigationService,
        private _tenantContext: TenantContextService,
        private _tenantsService: TenantsService
    ) {}

    get currentYear(): number {
        return new Date().getFullYear();
    }

    ngOnInit(): void {
        this._navigationService.navigation$
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((navigation: Navigation) => {
                this.navigation = navigation;
            });
        this._userService.user$
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((user: User) => {
                this.user = user;
                this.isSuperAdmin = user?.role === 'SUPER_ADMIN';
                if (this.isSuperAdmin) {
                    this.loadTenants();
                    this.restoreLastSelectedTenant();
                }
            });
        this._tenantContext.selectedTenant$
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((tenant: Tenant | null) => {
                this.selectedTenantId = tenant?.id || null;
            });
        this._fuseMediaWatcherService.onMediaChange$
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe(({ matchingAliases }) => {
                this.isScreenSmall = !matchingAliases.includes('md');
            });
    }

    ngOnDestroy(): void {
        this._unsubscribeAll.next(null);
        this._unsubscribeAll.complete();
    }

 
    toggleNavigation(name: string): void {
        const navigation =
            this._fuseNavigationService.getComponent<FuseVerticalNavigationComponent>(
                name
            );
        if (navigation) {
            navigation.toggle();
        }
    }

    private loadTenants(): void {
        this._tenantsService.getAllTenants().subscribe({
            next: (tenants) => {
                this.tenants = tenants;
                this.tenantOptions = tenants.map((t) => ({
                    value: t.id,
                    label: `${t.companyName} (${t.subdomain})`,
                }));
            },
            error: (error) => {
                console.error('Failed to load tenants:', error);
            },
        });
    }

    private restoreLastSelectedTenant(): void {
        const savedId = this._tenantContext.getSelectedTenantId();
        if (savedId && this.tenants.length > 0) {
            const tenant = this.tenants.find((t) => t.id === savedId);
            if (tenant) {
                this._tenantContext.selectTenant(tenant);
            }
        }
    }

    onTenantChange(tenantId: number | null): void {
        if (!tenantId) {
            this._tenantContext.clearTenantSelection();
            return;
        }
        const tenant = this.tenants.find((t) => t.id === tenantId);
        if (tenant) {
            this._tenantContext.selectTenant(tenant);
        }
    }
}
