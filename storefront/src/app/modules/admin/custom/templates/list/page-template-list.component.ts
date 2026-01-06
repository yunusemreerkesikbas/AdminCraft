import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal, TemplateRef, ViewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BaseCrudListComponent, CrudStore } from '@core/crud';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { GridAction, GridActionEvent, GridColumn, SpaAdminGridComponent } from '@shared/components/spa-admin-grid';
import { NotificationService } from '@shared/notifications/notification.service';
import { debounceTime, Observable, take, takeUntil } from 'rxjs';
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
    SpaAdminGridComponent
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PageTemplateListComponent extends BaseCrudListComponent<PageTemplate> {
  @ViewChild('slotsTemplate', { static: true }) slotsTemplate!: TemplateRef<any>;
  @ViewChild('statusTemplate', { static: true }) statusTemplate!: TemplateRef<any>;

  protected service = inject(PageTemplateService);
  protected store = new CrudStore<PageTemplate>();
  
  readonly #dialog = inject(MatDialog);
  readonly #notify = inject(NotificationService);
  readonly #transloco = inject(TranslocoService);

  // Pagination & Search
  searchInputControl = new FormControl('');
  pageIndexSig = signal(0);
  pageSizeSig = signal(10);

  totalItemsSig = computed(() => this.filtered().length);
  
  protected paginatedItemsSig = computed(() => {
      const items = this.filtered();
      const startIndex = this.pageIndexSig() * this.pageSizeSig();
      const endIndex = startIndex + this.pageSizeSig();
      return items.slice(startIndex, endIndex);
  });

  columns: GridColumn<PageTemplate>[] = [];
  actions: GridAction<PageTemplate>[] = [];

  constructor() {
      super();
      // Columns and actions initialized in ngOnInit or constructor? 
      // Templates are static: true, so available in ngOnInit.
      // But we can define them lazily or in constructor if they don't depend on templates (they do).
      // So use ngOnInit, but BaseCrudList implements ngOnInit.
      // We can override or simply init in ngOnInit.
  }

  override ngOnInit(): void {
      super.ngOnInit();
      this.initGrid();
      
      // Subscribe to search changes
      this.searchInputControl.valueChanges
          .pipe(takeUntil(this.destroy$), debounceTime(300))
          .subscribe((value) => {
              this.onSearchChange(value || '');
              this.pageIndexSig.set(0); // Reset to first page on search
          });
  }

  onPageChange(event: PageEvent): void {
      this.pageIndexSig.set(event.pageIndex);
      this.pageSizeSig.set(event.pageSize);
  }

  private initGrid(): void {
      this.columns = [
        {
            key: 'name',
            label: 'admin.common.fields.name',
            type: 'text',
            getSecondaryValue: (item) => item.uid
        },
        {
            key: 'description',
            label: 'admin.common.fields.description',
            type: 'text',
            hideOn: 'md'
        },
        {
            key: 'slots',
            label: 'admin.pageTemplates.slots.title',
            type: 'custom',
            template: this.slotsTemplate,
            hideOn: 'sm'
        },
        {
            key: 'status',
            label: 'admin.common.fields.status',
            type: 'custom',
            template: this.statusTemplate,
            width: '120px'
        }
      ];

      this.actions = [
          {
              action: 'edit',
              icon: 'heroicons_outline:pencil',
              label: 'admin.common.edit'
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

  protected override fetchItems(): Observable<PageTemplate[]> {
    return this.service.list();
  }

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
      this.service.delete(template.id).pipe(take(1)).subscribe({
        next: () => {
          this.#notify.success('admin.pageTemplates.messages.deleteSuccess');
          this.loadItems();
        },
        error: () => {
          this.#notify.alert('admin.pageTemplates.messages.deleteFailed');
        }
      });
    }
  }
}
