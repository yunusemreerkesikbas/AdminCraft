import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal, TemplateRef, ViewChild } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BaseCrudListComponent, CrudStore } from '@core/crud';
import { LanguageContextService } from '@core/services/language-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { SpaStatusBadgeComponent } from '@shared/components/custom-ui/spa-status-badge/spa-status-badge.component';
import { GridAction, GridActionEvent, GridColumn, SpaAdminGridComponent } from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { ConfirmationService } from '@shared/services/confirmation.service';
import { debounceTime, Observable, take, takeUntil } from 'rxjs';
import { PageBuilderService } from '../page-builder.service';
import { CreatePageRequest, PageListDto, UpdatePageRequest } from '../page-builder.types';
import { PageEditDialogComponent, PageEditDialogData } from '../page-edit-dialog/page-edit-dialog.component';
import { PageSlotDialogComponent, PageSlotDialogData } from '../slots/page-slot-dialog/page-slot-dialog.component';

@Component({
    selector: 'spa-page-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        MatInputModule,
        MatPaginatorModule,
        MatTooltipModule,
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaAdminGridComponent,
        SpaStatusBadgeComponent,
        SpaAdminPaginatorComponent
    ],
    templateUrl: './page-list.component.html',
    styles: [],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class PageListComponent extends BaseCrudListComponent<PageListDto, CreatePageRequest, UpdatePageRequest> {

    protected override service = inject(PageBuilderService);
    protected override store = new CrudStore<PageListDto>();
    #notificationService = inject(NotificationService);
    #confirmationService = inject(ConfirmationService);
    #dialog = inject(MatDialog);
    #languageContext = inject(LanguageContextService);

    protected pageSizeSig = signal(24);
    protected pageIndexSig = signal(0);
    protected searchInputControl = new FormControl('');
    
    protected totalItemsSig = computed(() => this.filtered().length);
    protected paginatedItemsSig = computed(() => {
        const items = this.filtered();
        const startIndex = this.pageIndexSig() * this.pageSizeSig();
        const endIndex = startIndex + this.pageSizeSig();
        return items.slice(startIndex, endIndex);
    });

    protected columns: GridColumn<PageListDto>[] = [];
    protected actions: GridAction<PageListDto>[] = [];
    
    @ViewChild('statusTemplate', { static: true }) statusTemplate!: TemplateRef<any>;

    get #supportedLanguages(): string[] {
        return this.#languageContext.supportedLanguages();
    }

    protected override onInit(): void {
        this.#setupSearchDebounce();
        this.#initGridConfig();
    }

    #initGridConfig(): void {
        this.columns = [
            {
                key: 'uid',
                label: 'admin.pages.fields.uid',
                type: 'text',
                width: '1fr'
            },
            {
                key: 'status',
                label: 'admin.common.grid.status',
                type: 'custom',
                template: this.statusTemplate,
                width: '120px',
                hideOn: 'sm'
            }
        ];

        this.actions = [
            {
                icon: 'heroicons_outline:pencil-square',
                label: 'admin.common.actions.edit',
                action: 'edit'
            },
            {
                icon: 'heroicons_outline:view-columns',
                label: 'admin.pages.actions.slots',
                action: 'slots'
            },
            {
                icon: 'heroicons_outline:trash',
                label: 'admin.common.actions.delete',
                action: 'delete',
                color: 'warn'
            }
        ];
    }

    protected override loadItems(): void {
        super.loadItems();
    }

    protected override fetchItems(): Observable<PageListDto[]> {
        return this.service.listPages();
    }

    #setupSearchDebounce(): void {
        this.searchInputControl.valueChanges.pipe(
            debounceTime(300),
            takeUntil(this.destroy$)
        ).subscribe(query => {
            this.onSearchChange(query || '');
        });
    }

    protected override onSearchChange(query: string): void {
        super.onSearchChange(query);
        this.pageIndexSig.set(0);
    }

    createPage(): void {
        this.#openPageDialog('create');
    }

    editPage(page: PageListDto): void {
        this.service.getPageDetail(page.id).pipe(take(1)).subscribe({
            next: (detail) => {
                this.#openPageDialog('edit', detail);
            },
            error: () => {
                this.#notificationService.alert('admin.pages.errors.loadFailed');
            }
        });
    }

    #openPageDialog(mode: 'create' | 'edit', page?: any): void {
        const dialogData: PageEditDialogData = {
            mode,
            page: page || undefined,
            languages: this.#supportedLanguages
        };

        const dialogRef = this.#dialog.open(PageEditDialogComponent, {
            width: '720px',
            height: '80vh',
            disableClose: true,
            data: dialogData
        });

        dialogRef.afterClosed().pipe(take(1)).subscribe(result => {
            if (result) {
                this.loadItems();
            }
        });
    }

    #openSlotDialog(page: PageListDto): void {
        const dialogData: PageSlotDialogData = {
            pageId: page.id,
            pageUid: page.uid,
            templateId: page.templateId ?? null,
        };

        this.#dialog.open(PageSlotDialogComponent, {
            width: '900px',
            height: '80vh',
            maxWidth: '95vw',
            panelClass: 'spa-dialog-panel',
            data: dialogData
        });
    }

    deletePage(page: PageListDto): void {
        const confirmation = this.#confirmationService.confirm(
            'admin.pages.dialogs.delete.title',
            'admin.pages.dialogs.delete.confirm'
        );
        confirmation.pipe(takeUntil(this.destroy$)).subscribe((result) => {
            if (result) {
                this.deleteItem(page);
            }
        });
    }

    protected onGridAction(event: GridActionEvent<PageListDto>): void {
        const { action, item } = event;
        switch (action) {
            case 'edit':
                this.editPage(item);
                break;
            case 'slots':
                this.#openSlotDialog(item);
                break;
            case 'delete':
                this.deletePage(item);
                break;
        }
    }

    protected override onDeleteSuccess(item: PageListDto): void {
        this.#notificationService.success('admin.common.messages.operationSuccess');
    }

    protected override onDeleteError(error: any): void {
         this.#notificationService.alert('admin.common.errors.server');
    }

    onPageChange(event: PageEvent): void {
        this.pageIndexSig.set(event.pageIndex);
        this.pageSizeSig.set(event.pageSize);
        this.loadItems();
    }
}
