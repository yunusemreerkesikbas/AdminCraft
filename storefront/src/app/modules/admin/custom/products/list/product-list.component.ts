import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    computed,
    inject,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule } from '@angular/material/paginator';
import { CrudHttpService, CrudStore } from '@core/crud';
import { BasePaginatedListComponent } from '@core/crud/base-paginated-list.component';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import {
    GridAction,
    GridColumn,
    SpaAdminGridComponent,
} from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { ConfirmationService } from '@shared/services/confirmation.service';
import { take } from 'rxjs';
import { ProductFieldDialogComponent } from '../fields/product-field-dialog/product-field-dialog.component';
import {
    ProductCompositeRequest,
    ProductListItemResponse,
} from '../models/product.types';
import { ProductEditDialogComponent } from '../product-edit-dialog/product-edit-dialog.component';
import { ProductFieldService } from '../services/product-field.service';
import { ProductService } from '../services/product.service';

@Component({
    selector: 'spa-product-list',
    templateUrl: './product-list.component.html',
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaAdminGridComponent,
        SpaAdminPaginatorComponent,
        MatButtonModule,
        MatIconModule,
        MatPaginatorModule,
        FormsModule,
        ReactiveFormsModule,
    ],
})
export class ProductListComponent extends BasePaginatedListComponent<
    ProductListItemResponse,
    ProductCompositeRequest,
    ProductCompositeRequest
> {
    protected override service = inject(
        ProductService
    ) as unknown as CrudHttpService<
        ProductListItemResponse,
        ProductCompositeRequest,
        ProductCompositeRequest
    >;
    protected override store = new CrudStore<ProductListItemResponse>();
    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;

    #productFieldService = inject(ProductFieldService);
    #dialog = inject(MatDialog);
    #confirmation = inject(ConfirmationService);
    #notification = inject(NotificationService);

    // Global Fields Signal
    protected fieldDefinitions = toSignal(
        this.#productFieldService.getAllDefinitions(),
        { initialValue: [] }
    );

    // Base Columns
    protected baseColumns: GridColumn<ProductListItemResponse>[] = [
        {
            key: 'name',
            label: 'admin.common.grid.name',
            type: 'text',
            getSecondaryValue: (item) => item.sku,
        },
        {
            key: 'productTypeName',
            label: 'admin.products.fields.type',
            type: 'text',
        },
        {
            key: 'price',
            label: 'admin.products.fields.price',
            type: 'custom',
            getValue: (item) => item.price?.formattedValue || '-',
        },
        {
            key: 'status',
            label: 'admin.common.grid.status',
            type: 'status',
            width: '120px',
        },
    ];

    // Computed Columns combining Base + Dynamic Global Fields
    protected columns = computed(() => {
        const definitions = this.fieldDefinitions();
        // Check if definitions is valid array before filtering
        if (!definitions || !Array.isArray(definitions)) {
            return this.baseColumns;
        }

        const globalFields = definitions
            .filter((f) => f.isVisibleInList)
            .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
            .map((f) => ({
                key: `custom_${f.code}`, // Use a prefix to distinguish
                label: f.name,
                type: 'text' as const,
                getValue: (item: ProductListItemResponse) => {
                    // Start of workaround: The backend response structure needs to change to return customFields flat or in a map
                    // For now, assuming item has a customFields map or we will adapt later
                    // This part depends on how `ProductListItemResponse` is updated in backend Phase 7
                    // Since backend update is pending on response DTO, we might not see values yet.
                    const customFields = (item as any).customFields as Record<
                        string,
                        any
                    >;
                    return customFields?.[f.code] || '-';
                },
            }));

        return [...this.baseColumns, ...globalFields];
    });

    protected actions: GridAction<ProductListItemResponse>[] = [
        {
            icon: 'heroicons_outline:pencil-square',
            label: 'admin.common.actions.edit',
            action: 'edit',
        },
        {
            icon: 'heroicons_outline:trash',
            label: 'admin.common.actions.delete',
            action: 'delete',
            color: 'warn',
        },
    ];

    protected override onInit(): void {
        // Base class overrides
    }

    protected onGridAction(event: {
        action: string;
        item: ProductListItemResponse;
    }): void {
        if (event.action === 'edit') {
            this.editProduct(event.item);
        } else if (event.action === 'delete') {
            this.deleteProduct(event.item);
        }
    }

    createProduct(): void {
        this.#openDialog('create');
    }

    editProduct(item: ProductListItemResponse): void {
        this.#openDialog('edit', item);
    }

    deleteProduct(item: ProductListItemResponse): void {
        this.#confirmation
            .confirm(
                'admin.products.delete.title',
                'admin.products.delete.message'
            )
            .pipe(take(1))
            .subscribe((confirmed) => {
                if (confirmed) {
                    this.deleteItem(item);
                }
            });
    }

    addField(): void {
        this.#dialog
            .open(ProductFieldDialogComponent, {
                width: '600px',
                maxWidth: '95vw',
                disableClose: true,
                data: { mode: 'create' },
            })
            .afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (result) {
                    // Refresh definitions to update columns
                    // Since we use toSignal on the observable, we might need a way to trigger refresh.
                    // A better way would be using a signal in the service or just manual subscription here?
                    // For simplicity, let's re-assign the observable signal (hacky) or better, use a trigger signal.
                    // Ideally, the service should expose a signal.
                    // Let's just reload the page for now or implement a refresh trigger if needed.
                    // Or better: manual implementation of signal with refresh logic.
                    window.location.reload(); // Temporary quick refresh
                }
            });
    }

    #openDialog(mode: 'create' | 'edit', item?: ProductListItemResponse): void {
        // Need to pass ID if editing, dialog will fetch details
        const data = { mode, productId: item?.id };

        this.#dialog
            .open(ProductEditDialogComponent, {
                width: '1000px',
                height: '90vh',
                maxWidth: '95vw',
                disableClose: true,
                data: data,
            })
            .afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (result) {
                    this.loadItems();
                }
            });
    }

    protected override onDeleteSuccess(item: ProductListItemResponse): void {
        this.#notification.success('admin.common.messages.deleteSuccess');
    }

    protected override onDeleteError(error: any): void {
        this.#notification.alert(
            error?.error?.message || 'admin.common.errors.deleteFailed'
        );
    }
}
