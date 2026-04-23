import { ChangeDetectionStrategy, Component, computed, inject, signal, TemplateRef, ViewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BaseSelectablePaginatedListComponent, BulkDeleteRunnerService } from '@core/crud';
import { UserService } from '@core/user/user.service';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { GridAction, GridActionEvent, GridColumn, SpaAdminGridComponent } from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { SpaAdminSortDropdownComponent } from '@shared/components/spa-admin-sort-dropdown/spa-admin-sort-dropdown.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { VIEWER_ROLE } from '@shared/constants';
import { ConfirmationService } from '@shared/services/confirmation.service';
import { finalize, take, takeUntil } from 'rxjs';
import { PageTemplateEditDialogComponent } from '../edit-dialog/page-template-edit-dialog.component';
import { PageTemplateService } from '../page-template.service';
import { PageTemplateStore } from '../page-template.store';
import { PageTemplate } from '../page-template.types';

@Component({
  selector: 'spa-page-template-list',
  templateUrl: './page-template-list.component.html',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatChipsModule,
    MatTooltipModule,
    TranslocoModule,
    AdminPageHeaderComponent,
    SpaAdminGridComponent,
    SpaAdminPaginatorComponent,
    SpaAdminSortDropdownComponent,
    MatCheckboxModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PageTemplateListComponent extends BaseSelectablePaginatedListComponent<PageTemplate> {
  @ViewChild('slotsTemplate', { static: true }) slotsTemplate!: TemplateRef<any>;
  @ViewChild('statusTemplate', { static: true }) statusTemplate!: TemplateRef<any>;
  @ViewChild('headerSelectAllTemplate', { static: true }) headerSelectAllTemplate!: TemplateRef<any>;
  @ViewChild('rowCheckboxTemplate', { static: true }) rowCheckboxTemplate!: TemplateRef<any>;

  protected override service = inject(PageTemplateService);
  protected override store = inject(PageTemplateStore);
  
  readonly #dialog = inject(MatDialog);
  readonly #notify = inject(NotificationService);
  readonly #confirmationService = inject(ConfirmationService);
  readonly #userService = inject(UserService);
  readonly #bulkDeleteRunner = inject(BulkDeleteRunnerService);

  protected override readonly defaultSort = 'id,desc';
  protected override readonly defaultPageSize = 10;

  protected searchInputControl = new FormControl('');
  protected readonly selectedIdsSig = this.store.selectedIdsSig;
  protected readonly selectedCountSig = this.store.selectedCountSig;
  protected readonly isViewerSig = computed(() => this.#userService.user()?.role === VIEWER_ROLE);

  protected paginatedItemsSig = computed(() => this.store.items());

  columns: GridColumn<PageTemplate>[] = [];
  actions: GridAction<PageTemplate>[] = [];

  protected override onInit(): void {
    this.initGrid();
    
    this.searchInputControl.valueChanges
          .pipe(takeUntil(this.destroy$))
          .subscribe((value) => {
              this.onSearchInput(value || '');
          });
  }


  private initGrid(): void {
      const baseColumns: GridColumn<PageTemplate>[] = [
        {
            key: 'uid',
            label: 'admin.common.grid.uid',
            type: 'text',
            width: '250px'
        },
        {
            key: 'slots',
            label: 'admin.pageTemplates.slots.title',
            type: 'custom',
            template: this.slotsTemplate,
            width: '1fr',
            hideOn: 'sm'
        }
      ];
      this.columns = this.isViewerSig()
        ? baseColumns
        : [
            {
              key: 'select',
              label: '',
              type: 'custom',
              width: '52px',
              cssClass: 'flex items-center justify-center',
              headerTemplate: this.headerSelectAllTemplate,
              template: this.rowCheckboxTemplate,
            },
            ...baseColumns,
          ];

      this.actions = [
          {
              action: 'edit',
              icon: 'heroicons_outline:pencil-square',
              label: 'admin.common.edit',
              show: (item) => !item.isSystem
          },
          {
              action: 'delete',
              icon: 'heroicons_outline:trash',
              label: 'admin.common.delete',
              color: 'warn',
              show: (item) => !item.isSystem
          }
      ];
  }

  // fetchItems is implemented by BasePaginatedList using service.listPaged

  protected onGridAction(event: GridActionEvent<PageTemplate>): void {
      switch (event.action) {
          case 'edit':
              this.editTemplate(event.item);
              break;
          case 'delete':
              this.deleteTemplate(event.item);
              break;
      }
  }

  protected createTemplate(): void {
    if (this.isViewerSig()) {
      return;
    }
    const dialogRef = this.#dialog.open(PageTemplateEditDialogComponent, {
      width: '800px',
      maxWidth: '95vw',
      height: '80vh',
      data: { mode: 'create' },
      panelClass: 'spa-dialog-panel'
    });

    dialogRef.afterClosed().pipe(take(1)).subscribe((result: boolean | undefined) => {
      if (result) {
        this.loadItems();
      }
    });
  }

  protected editTemplate(template: PageTemplate): void {
    if (this.isViewerSig()) {
      return;
    }
    const dialogRef = this.#dialog.open(PageTemplateEditDialogComponent, {
      width: '800px',
      maxWidth: '95vw',
      height: '80vh',
      data: { mode: 'edit', template },
      panelClass: 'spa-dialog-panel'
    });

    dialogRef.afterClosed().pipe(take(1)).subscribe((result: boolean | undefined) => {
      if (result) {
        this.loadItems();
      }
    });
  }

  protected deleteTemplate(template: PageTemplate): void {
    if (this.isViewerSig()) {
      return;
    }
    this.#confirmationService
      .confirm(
        'admin.common.delete',
        'admin.pageTemplates.confirmDelete',
        'admin.common.actions.confirm',
        'warning',
        { name: template.name }
      )
      .pipe(take(1))
      .subscribe((confirmed) => {
        if (confirmed) {
          this.deleteItem(template);
        }
      });
  }

  protected showSelectionUi(): boolean {
    return !this.isViewerSig();
  }

  protected canSelectTemplate(item: PageTemplate): boolean {
    return !item.isSystem;
  }

  protected override canSelect(item: PageTemplate): boolean {
    return this.canSelectTemplate(item);
  }

  protected pageSelectionState(): { allSelected: boolean; indeterminate: boolean } {
    return super.pageSelectionState();
  }

  protected onToggleSelectAllOnPage(event: { checked: boolean }): void {
    this.toggleSelectAllOnPage(event.checked);
  }

  protected onRowCheckboxChange(item: PageTemplate): void {
    if (!this.canSelectTemplate(item)) {
      return;
    }
    this.store.toggleSelected(item.id);
  }

  protected deleteSelectedTemplates(): void {
    if (this.isViewerSig()) {
      return;
    }
    const ids = this.selectedIdsSig();
    if (ids.length === 0) {
      return;
    }
    this.#confirmationService
      .confirm(
        'admin.pageTemplates.bulkDeleteConfirmTitle',
        'admin.pageTemplates.bulkDeleteConfirmMessage',
        'admin.common.actions.confirm',
        'warning',
        { count: ids.length }
      )
      .pipe(take(1))
      .subscribe((confirmed) => {
        if (!confirmed) {
          return;
        }
        this.store.setLoading(true);
        this.#bulkDeleteRunner
          .run(ids, {
            bulkDelete$: () =>
              this.service.bulkDeleteTemplatesWithResponse(ids).pipe(take(1)),
          })
          .pipe(
            finalize(() => this.store.setLoading(false)),
            takeUntil(this.destroy$)
          )
          .subscribe({
            next: (result) => {
              if (result.message) {
                this.#notify.success(result.message);
              }
              this.store.clearSelection();
              this.loadItems();
            },
            error: (error) => {
              const msg = error?.error?.message ?? error?.message ?? '';
              this.#notify.alert(msg);
              this.loadItems();
            },
          });
      });
  }
  
  protected override onDeleteSuccess(item: PageTemplate): void {
      this.#notify.success('admin.pageTemplates.messages.deleteSuccess');
      this.store.clearPageSelection([item.id]);
      this.loadItems();
  }

  protected override onDeleteError(error: any): void {
    this.#notify.alert('admin.pageTemplates.messages.deleteFailed');
  }

  onSortDropdownChange(sortCode: string): void {
      this.onSortChange(sortCode);
  }
}
