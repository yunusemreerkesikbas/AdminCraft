import { ChangeDetectionStrategy, Component, inject, Input, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { UserService } from 'app/core/user/user.service';
import { TenantsService } from 'app/modules/admin/custom/tenants/tenants.service';
import { Tenant } from 'app/modules/admin/custom/tenants/tenants.types';
import { SpaSelectComponent, SpaSelectOption } from 'app/shared/components/custom-ui/spa-select/spa-select.component';
import { Subject, take, takeUntil } from 'rxjs';

@Component({
    selector: 'spa-tenant-selector',
    templateUrl: './tenant-selector.component.html',
    imports: [SpaSelectComponent, FormsModule],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TenantSelectorComponent implements OnInit, OnDestroy {
    @Input() label = 'Tenant';
    @Input() placeholder = 'Select a tenant...';
    @Input() styleClasses = '';

    protected isSuperAdmin = false;
    protected tenantOptions: SpaSelectOption<number>[] = [];
    protected selectedTenantId: number | null = null;

    #tenants: Tenant[] = [];
    #snackBar = inject(MatSnackBar);
    #userService = inject(UserService);
    #tenantContext = inject(TenantContextService);
    #tenantsService = inject(TenantsService);
    #destroy$ = new Subject<void>();

    ngOnInit(): void {
        this.#initializeUserSubscription();
        this.#initializeTenantSelectionSubscription();
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    protected onTenantChange(tenantId: number | null): void {
        if (!tenantId) {
            this.#tenantContext.clearTenantSelection();
            return;
        }
        const tenant = this.#tenants.find((t) => t.id === tenantId);
        if (tenant) {
            this.#tenantContext.selectTenant(tenant);
        }
    }

    #initializeUserSubscription(): void {
        this.#userService.user$
            .pipe(takeUntil(this.#destroy$))
            .subscribe((user) => {
                this.isSuperAdmin = user?.role === 'SUPER_ADMIN';
                if (this.isSuperAdmin) {
                    this.#loadTenants();
                }
            });
    }

    #initializeTenantSelectionSubscription(): void {
        this.#tenantContext.selectedTenant$
            .pipe(takeUntil(this.#destroy$))
            .subscribe((tenant: Tenant | null) => {
                this.selectedTenantId = tenant?.id || null;
            });
    }

    #loadTenants(): void {
        this.#tenantsService.getAllTenants()
            .pipe(take(1))
            .subscribe({
                next: (tenants) => {
                    this.#tenants = tenants;
                    this.tenantOptions = tenants.map((t) => ({
                        value: t.id,
                        label: `${t.companyName} (${t.subdomain})`,
                    }));
                    this.#restoreLastSelectedTenant();
                },
                error: () => {
                    this.#snackBar.open('Failed to load tenants', 'Close', { duration: 3000 });
                },
            });
    }

    #restoreLastSelectedTenant(): void {
        const savedId = this.#tenantContext.getSelectedTenantId();
        if (savedId && this.#tenants.length > 0) {
            const tenant = this.#tenants.find((t) => t.id === savedId);
            if (tenant) {
                this.#tenantContext.selectTenant(tenant);
            }
        }
    }
}
