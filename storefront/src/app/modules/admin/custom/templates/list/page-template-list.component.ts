import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, TemplateRef, ViewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BasePaginatedListComponent, CrudStore } from '@core/crud';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { GridAction, GridActionEvent, GridColumn, SpaAdminGridComponent } from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { SpaAdminSortDropdownComponent } from '@shared/components/spa-admin-sort-dropdown/spa-admin-sort-dropdown.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { take, takeUntil } from 'rxjs';
import { PageTemplateEditDialogComponent } from '../edit-dialog/page-template-edit-dialog.component';
import { PageTemplateService } from '../page-template.service';
import { PageTemplate } from '../page-template.types';

@Component({
  selector: 'spa-page-template-list',
  templateUrl: './page-template-list.component.html',
  standalone: true,
  imports: [
    CommonModule,
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
    SpaAdminSortDropdownComponent
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PageTemplateListComponent extends BasePaginatedListComponent<PageTemplate> {
  @ViewChild('slotsTemplate', { static: true }) slotsTemplate!: TemplateRef<any>;
  @ViewChild('statusTemplate', { static: true }) statusTemplate!: TemplateRef<any>;

  protected override service = inject(PageTemplateService);
  protected override store = new CrudStore<PageTemplate>();
  
  readonly #dialog = inject(MatDialog);
  readonly #notify = inject(NotificationService);
  readonly #transloco = inject(TranslocoService);

  // BasePaginatedList requires these
  protected override readonly defaultSort = 'id,desc';
  protected override readonly defaultPageSize = 10;

  // We can just alias the base searchInput$ via FormControl binding or use base logic
  // MediaList uses its own FormControl and subscribes. 
  // BasePaginatedList uses #searchInput$ subject.
  // We'll follow MediaList pattern:
  searchInputControl = new FormControl('');

  // Computed from store (which now holds paged items)
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
      this.columns = [
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

      this.actions = [
          {
              action: 'edit',
              icon: 'heroicons_outline:pencil',
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
    const confirmMessage = this.#transloco.translate('admin.pageTemplates.confirmDelete', { name: template.name });
    if (confirm(confirmMessage)) {
      this.deleteItem(template); // Use Base method which handles loading/error
    }
  }
  
  // Implement success/error callbacks for delete if custom notification needed
  protected override onDeleteSuccess(item: PageTemplate): void {
      this.#notify.success('admin.pageTemplates.messages.deleteSuccess');
      this.loadItems(); // Reload to refresh page state? Base deleteItem removes from store but for paged list usually we might want to reload if page becomes empty or count changes? 
      // Base deleteItem: this.store.removeItem(item.id);
      // But if we want to ensure total counts are correct from server, explicit reload is sometimes better.
      // But let's stick to base behavior or reload.
      this.loadItems();
  }

  protected override onDeleteError(error: any): void {
    this.#notify.alert('admin.pageTemplates.messages.deleteFailed');
  }
  
  onSortDropdownChange(sortCode: string): void {
      this.onSortChange(sortCode);
  }
}
