import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal, TemplateRef, ViewChild } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router } from '@angular/router';
import { BaseCrudListComponent, CrudStore } from '@core/crud';
import { LanguageContextService } from '@core/services/language-context.service';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { SpaStatusBadgeComponent } from '@shared/components/custom-ui/spa-status-badge/spa-status-badge.component';
import { GridAction, GridActionEvent, GridColumn, SpaAdminGridComponent } from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { ConfirmationService } from '@shared/services/confirmation.service';
import { ItemDialogService } from '@shared/services/item-dialog.service';
import { ItemDialogOptions } from '@shared/types/item-dialog.types';
import { debounceTime, forkJoin, Observable, take, takeUntil } from 'rxjs';
import { CreatePageFormData, PageI18nFormData } from '../models/page-form.types';
import { PageBuilderService } from '../page-builder.service';
import { CreatePageRequest, Language, PageI18nRequest, PageListDto, UpdatePageRequest } from '../page-builder.types';
import { PageSchemaBuilderService } from '../services/page-schema-builder.service';

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
    #router = inject(Router);
    #notificationService = inject(NotificationService);
    #confirmationService = inject(ConfirmationService);

    #itemDialogService = inject(ItemDialogService);
    #schemaBuilder = inject(PageSchemaBuilderService);
    #tenantContext = inject(TenantContextService);
    #languageContext = inject(LanguageContextService);

    protected pageSizeSig = signal(24);
    protected pageIndexSig = signal(0);
    protected searchInputControl = new FormControl('');
    
    // Derived signals for local pagination
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
        // Use BaseCrudList logic: fetchItems() -> store.setItems()
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
        const tenantId = this.#tenantContext.getCurrentTenantId();
        if (!tenantId) {
            this.#notificationService.warning('admin.pageBuilder.errors.noTenant');
            return;
        }

        const schema = this.#schemaBuilder.buildPageCreateSchema();
        const initial: CreatePageFormData = {
            status: 'DRAFT',
            sortOrder: 0
        };

        this.#supportedLanguages.forEach(lang => {
            initial[lang] = {};
        });

        const options: ItemDialogOptions<CreatePageFormData> = {
            titleKey: 'admin.dialog.title.create',
            mode: 'create',
            schema,
            languages: this.#supportedLanguages,
            initial,
            modalData: {
                disableClose: true,
                width: '720px',
                height: '80vh'
            }
        };

        this.#itemDialogService.open(options).pipe(take(1)).subscribe(result => {
            if (!result) return;

            try {
                const generalReq: CreatePageRequest = {
                    status: result.status || 'DRAFT',
                    sortOrder: result.sortOrder || 0,
                    styleClasses: result.styleClasses || null,
                    featuredImage: null
                };

                this.service.createPage(generalReq).pipe(take(1)).subscribe({
                    next: (createdPage) => {
                        const i18nUpdates: Observable<PageI18nRequest>[] = [];

                        this.#supportedLanguages.forEach(lang => {
                            const langData = result[lang] as PageI18nFormData | undefined;
                            const hasContent = langData && (
                                langData.urlPath ||
                                langData.title ||
                                langData.subtitle ||
                                langData.metaTitle ||
                                langData.metaDescription ||
                                langData.description
                            );

                            if (hasContent && langData) {
                                const i18nReq: PageI18nRequest = {
                                    language: lang.toUpperCase() as Language,
                                    urlPath: langData.urlPath || null,
                                    title: langData.title || null,
                                    subtitle: langData.subtitle || null,
                                    metaTitle: langData.metaTitle || null,
                                    metaDescription: langData.metaDescription || null,
                                    description: langData.description || null,
                                    status: result.status || 'DRAFT'
                                };
                                i18nUpdates.push(this.service.updatePageI18n(createdPage.id, lang.toUpperCase() as Language, i18nReq));
                            }
                        });

                        const handleSuccess = () => {
                            this.#notificationService.success('admin.pageBuilder.messages.pageCreated');
                            this.loadItems();
                        };

                        if (i18nUpdates.length > 0) {
                            forkJoin(i18nUpdates).pipe(take(1)).subscribe({
                                next: handleSuccess,
                                error: (err) => {
                                    this.#notificationService.alert('admin.pageBuilder.errors.creationFailed');
                                    this.loadItems();
                                }
                            });
                        } else {
                            handleSuccess();
                        }
                    },
                    error: (error) => {
                        this.#notificationService.alert('admin.pageBuilder.errors.creationFailed');
                    }
                });
            } catch (err) {
                this.#notificationService.alert('admin.pageBuilder.errors.creationFailed');
            }
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
                this.#router.navigate(['/admin/pages', item.uid]);
                break;
            case 'slots':
                this.#router.navigate(['/admin/pages', item.uid, 'slots']);
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
