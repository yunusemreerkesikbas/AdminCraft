import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    inject,
} from '@angular/core';
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
import { SpaAdminSortDropdownComponent } from '@shared/components/spa-admin-sort-dropdown/spa-admin-sort-dropdown.component';
import { take } from 'rxjs';
import {
    ProductCompositeRequest,
    ProductListItemResponse,
} from '../models/product.types';
import { ProductFieldDialogComponent } from '../fields/product-field-dialog/product-field-dialog.component';
import { ProductEditDialogComponent } from '../product-edit-dialog/product-edit-dialog.component';
import { ProductFieldService } from '../services/product-field.service';
import { ProductService } from '../services/product.service';
import { ProductVariantOptionsDialogComponent } from '../variant-options/product-variant-options-dialog.component';

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
        SpaAdminSortDropdownComponent,
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

    #matDialog = inject(MatDialog);
    #confirmationService = inject(ConfirmationService);
    #notificationService = inject(NotificationService);
    #productFieldService = inject(ProductFieldService);

    protected columns: GridColumn<ProductListItemResponse>[] = [
        {
            key: 'name',
            label: 'admin.common.grid.name',
            type: 'text',
            getSecondaryValue: (item) => item.sku,
            width: '1fr',
            cssClass: 'flex flex-col justify-center',
        },
        {
            key: 'productTypeName',
            label: 'admin.products.fields.type',
            type: 'text',
            width: '200px',
            cssClass: 'flex flex-col justify-center',
        },
        {
            key: 'status',
            label: 'admin.common.grid.status',
            type: 'status',
            width: '120px',
        },
    ];

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
        this.#confirmationService
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

    #openDialog(mode: 'create' | 'edit', item?: ProductListItemResponse): void {
        // Need to pass ID if editing, dialog will fetch details
        const data = { mode, productId: item?.id };

        this.#matDialog
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
        this.#notificationService.success('admin.common.messages.deleteSuccess');
    }

    protected override onDeleteError(error: any): void {
        this.#notificationService.alert(
            error?.error?.message || 'admin.common.errors.deleteFailed'
        );
    }

    openFieldDialog(): void {
        const dialogRef = this.#matDialog.open(ProductFieldDialogComponent, {
            data: { mode: 'create' },
            width: '600px',
        });

        dialogRef.afterClosed().pipe(take(1)).subscribe((result) => {
            if (result) {
                this.#productFieldService
                    .create(result)
                    .pipe(take(1))
                    .subscribe({
                        next: () => {
                            this.#notificationService.success(
                                'admin.common.messages.saveSuccess'
                            );
                        },
                        error: () => {
                            this.#notificationService.alert(
                                'admin.common.errors.saveFailed'
                            );
                        },
                    });
            }
            });
    }

    openVariantOptionsDialog(): void {
        this.#matDialog.open(ProductVariantOptionsDialogComponent, {
            width: '980px',
            maxWidth: '95vw',
        });
    }
}
