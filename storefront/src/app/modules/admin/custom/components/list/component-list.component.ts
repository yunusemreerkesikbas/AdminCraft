import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { BaseCrudListComponent, CrudStore } from '@core/crud';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaSelectComponent, SpaSelectOption } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { ItemDialogService } from '@shared/services/item-dialog.service';
import type { ItemDialogOptions } from '@shared/types/item-dialog.types';
import { forkJoin, take, takeUntil } from 'rxjs';
import { LanguageContextService } from '@core/services/language-context.service';
import { TranslocoService } from '@jsverse/transloco';
import { ComponentLibraryService } from '../services/component-library.service';
import { ComponentSchemaBuilderService } from '../services/component-schema-builder.service';
import { ComponentTypesManagerComponent } from '../types/component-types-manager.component';
import { ComponentDto, ComponentDetailDto, ComponentI18nRequest, ComponentStatus, ComponentTypeDto, CreateComponentRequest, UpdateComponentRequest } from '../models/component-library.types';
import { CreateComponentFormData, EditComponentFormData, isComponentI18nFormData } from '../models/component-form.types';

@Component({
    selector: 'spa-component-list',
    templateUrl: './component-list.component.html',
    styleUrls: ['./component-list.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        TranslocoModule,
        SpaSelectComponent
    ]
})
export class ComponentListComponent extends BaseCrudListComponent<ComponentDto, CreateComponentRequest, UpdateComponentRequest> {
    protected service = inject(ComponentLibraryService);
    protected store = new CrudStore<ComponentDto>();

    #notify = inject(NotificationService);
    #componentService = inject(ComponentLibraryService);
    #tenantCtx = inject(TenantContextService);
    #langCtx = inject(LanguageContextService);
    #dialog = inject(ItemDialogService);
    #schema = inject(ComponentSchemaBuilderService);
    #matDialog = inject(MatDialog);
    #transloco = inject(TranslocoService);

    protected tenantId?: number;
    componentTypes = signal<ComponentTypeDto[]>([]);
    supportedLanguages = signal<string[]>([]);

    selectedTypeId = signal<number | null>(null);
    selectedStatus = signal<ComponentStatus | null>(null);
    searchTerm = signal<string>('');

    typeOptions = computed<SpaSelectOption<number | null>[]>(() => [
        { value: null, label: this.#transloco.translate('admin.components.filters.allTypes') },
        ...this.componentTypes().map(t => ({ value: t.id, label: t.name }))
    ]);

    statusOptions: SpaSelectOption<ComponentStatus | null>[] = [
        { value: null, label: this.#transloco.translate('admin.components.filters.allStatus') },
        { value: ComponentStatus.ACTIVE, label: this.#transloco.translate('admin.common.status.active') },
        { value: ComponentStatus.DRAFT, label: this.#transloco.translate('admin.common.status.draft') },
        { value: ComponentStatus.INACTIVE, label: this.#transloco.translate('admin.common.status.inactive') }
    ];

    filteredComponents = computed(() => {
        let items = this.store.items();
        if (this.selectedTypeId()) {
            items = items.filter(c => c.componentTypeId === this.selectedTypeId());
        }
        if (this.selectedStatus()) {
            items = items.filter(c => c.status === this.selectedStatus());
        }
        if (this.searchTerm()) {
            const term = this.searchTerm().toLowerCase();
            items = items.filter(c =>
                c.name.toLowerCase().includes(term) ||
                c.code.toLowerCase().includes(term)
            );
        }
        return items;
    });

    protected override onInit(): void {
        const storedId = this.#tenantCtx.getCurrentTenantId();
        if (!storedId) {
            this.#notify.warning('admin.components.errors.noTenant');
            return;
        }
        this.tenantId = storedId;
        this.#loadTenantLanguages();
        this.#loadComponentTypes();
        this.loadItems();

        this.#tenantCtx.tenant$.pipe(takeUntil(this.destroy$)).subscribe((t) => {
            if (!t) return;
            if (t.id !== this.tenantId) {
                this.tenantId = t.id;
                this.#loadTenantLanguages();
                this.#loadComponentTypes();
                this.loadItems();
            }
        });
    }

    #loadTenantLanguages(): void {
        this.#langCtx.supportedLanguages$
            .pipe(takeUntil(this.destroy$))
            .subscribe((languages) => {
                this.supportedLanguages.set(languages || ['tr', 'en']);
            });
    }

    #loadComponentTypes(): void {
        this.#componentService.listComponentTypes()
            .pipe(take(1), takeUntil(this.destroy$))
            .subscribe({
                next: (types) => this.componentTypes.set(types),
                error: () => this.#notify.alert('admin.components.errors.loadTypesFailed')
            });
    }

    protected override beforeLoad(): boolean {
        if (!this.tenantId) {
            this.#notify.warning('admin.components.errors.noTenant');
            return false;
        }
        return true;
    }

    protected override fetchItems() {
        return this.#componentService.listComponents();
    }

    protected override onLoadError(error: any): void {
        this.#notify.alert('admin.components.errors.loadFailed');
    }

    onTypeFilterChange(typeId: number | null): void {
        this.selectedTypeId.set(typeId);
    }

    onStatusFilterChange(status: ComponentStatus | null): void {
        this.selectedStatus.set(status);
    }

    onSearchChange(term: string): void {
        this.searchTerm.set(term);
    }

    createComponent(): void {
        if (!this.tenantId || this.componentTypes().length === 0) {
            this.#notify.warning('admin.components.errors.noTypes');
            return;
        }

        const schema = this.#schema.buildComponentCreateSchema(this.componentTypes());
        const initial: CreateComponentFormData = {
            componentTypeId: null,
            code: null,
            name: null,
            status: ComponentStatus.DRAFT,
            order: 0,
            isVisible: true,
            styleClasses: null
        };

        const i18nInitial: Record<string, any> = {};
        this.supportedLanguages().forEach((lang) => {
            i18nInitial[lang] = {
                title: '',
                subtitle: '',
                description: '',
                imageUrl: '',
                imageAlt: '',
                buttonText: '',
                buttonUrl: '',
                buttonStyle: '',
                links: []
            };
        });

        const options: ItemDialogOptions<CreateComponentFormData> = {
            titleKey: 'admin.dialog.title.create',
            mode: 'create',
            schema,
            languages: this.supportedLanguages(),
            initial,
            i18nInitial,
            modalData: { disableClose: true, width: '800px', height: '80vh' }
        };

        this.#dialog.open(options).pipe(take(1)).subscribe((result) => {
            if (!result) return;

            const basePayload: CreateComponentRequest = {
                componentTypeId: result.componentTypeId!,
                code: result.code!,
                name: result.name!,
                status: result.status || ComponentStatus.DRAFT,
                baseData: {
                    order: result.order ?? 0,
                    isVisible: result.isVisible ?? true,
                    styleClasses: result.styleClasses || undefined
                }
            };

            this.#componentService.createComponent(basePayload)
                .pipe(take(1), takeUntil(this.destroy$))
                .subscribe({
                    next: (created) => {
                        this.#saveI18nForComponent(created.id, result, true);
                    },
                    error: () => this.#notify.alert('admin.components.errors.createFailed')
                });
        });
    }

    editComponent(componentId: number): void {
        if (!this.tenantId) {
            this.#notify.warning('admin.components.errors.noTenant');
            return;
        }

        this.store.setLoading(true);
        this.#componentService.getComponentDetail(componentId)
            .pipe(take(1), takeUntil(this.destroy$))
            .subscribe({
                next: (detail) => {
                    this.store.setLoading(false);
                    this.#openEditDialog(detail);
                },
                error: () => {
                    this.store.setLoading(false);
                    this.#notify.alert('admin.components.errors.loadDetailFailed');
                }
            });
    }

    #openEditDialog(detail: ComponentDetailDto): void {
        const componentType = this.componentTypes().find(t => t.id === detail.componentTypeId);
        const schema = this.#schema.buildComponentEditSchema(this.componentTypes());
        const initial: EditComponentFormData = {
            componentTypeId: detail.componentTypeId,
            code: detail.code,
            name: detail.name,
            status: detail.status,
            order: detail.baseData.order ?? 0,
            isVisible: detail.baseData.isVisible ?? true,
            styleClasses: detail.baseData.styleClasses || null
        };

        const i18nInitial: Record<string, any> = {};
        this.supportedLanguages().forEach((lang) => {
            const translation = detail.translations?.find(t => t.language === lang);
            i18nInitial[lang] = {
                title: translation?.baseLocalizedData.title || '',
                subtitle: translation?.baseLocalizedData.subtitle || '',
                description: translation?.baseLocalizedData.description || '',
                imageUrl: translation?.baseLocalizedData.imageUrl || '',
                imageAlt: translation?.baseLocalizedData.imageAlt || '',
                buttonText: translation?.baseLocalizedData.buttonText || '',
                buttonUrl: translation?.baseLocalizedData.buttonUrl || '',
                buttonStyle: translation?.baseLocalizedData.buttonStyle || '',
                links: translation?.baseLocalizedData.links || [],
                ...(translation?.extendedLocalizedData || {})
            };
        });

        const options: ItemDialogOptions<EditComponentFormData, number> = {
            titleKey: 'admin.dialog.title.edit',
            mode: 'edit',
            schema,
            languages: this.supportedLanguages(),
            initial,
            i18nInitial,
            id: detail.id,
            modalData: { disableClose: true, width: '800px', height: '80vh' },
            extendedFieldsSchema: componentType?.extendedFieldsSchema
        };

        this.#dialog.open(options)
            .pipe(take(1))
            .subscribe((result) => {
                if (!result) return;

                const basePayload: UpdateComponentRequest = {
                    name: result.name!,
                    status: result.status || ComponentStatus.DRAFT,
                    baseData: {
                        order: result.order ?? 0,
                        isVisible: result.isVisible ?? true,
                        styleClasses: result.styleClasses || undefined
                    }
                };

                this.#componentService.updateComponent(detail.id, basePayload)
                    .pipe(take(1), takeUntil(this.destroy$))
                    .subscribe({
                        next: () => {
                            this.#saveI18nForComponent(detail.id, result, false);
                        },
                        error: () => this.#notify.alert('admin.components.errors.updateFailed')
                    });
            });
    }

    #saveI18nForComponent(componentId: number, formData: any, isCreate: boolean): void {
        const i18nRequests = this.supportedLanguages().map((lang) => {
            const langData = formData[lang];
            if (!isComponentI18nFormData(langData)) return null;

            const baseFields = ['title', 'subtitle', 'description', 'imageUrl', 'imageAlt', 'buttonText', 'buttonUrl', 'buttonStyle', 'links'];
            const extendedLocalizedData: Record<string, any> = {};
            Object.keys(langData).forEach(key => {
                if (!baseFields.includes(key)) {
                    extendedLocalizedData[key] = langData[key];
                }
            });

            const request: ComponentI18nRequest = {
                baseLocalizedData: {
                    title: langData.title || undefined,
                    subtitle: langData.subtitle || undefined,
                    description: langData.description || undefined,
                    imageUrl: langData.imageUrl || undefined,
                    imageAlt: langData.imageAlt || undefined,
                    buttonText: langData.buttonText || undefined,
                    buttonUrl: langData.buttonUrl || undefined,
                    buttonStyle: langData.buttonStyle || undefined,
                    links: langData.links || undefined
                },
                extendedLocalizedData: Object.keys(extendedLocalizedData).length > 0 ? extendedLocalizedData : undefined,
                status: ComponentStatus.DRAFT
            };

            return this.#componentService.updateComponentI18n(componentId, lang, request);
        }).filter(req => req !== null);

        if (i18nRequests.length === 0) {
            this.#notify.success(isCreate ? 'admin.components.success.created' : 'admin.components.success.updated');
            this.loadItems();
            return;
        }

        forkJoin(i18nRequests)
            .pipe(take(1), takeUntil(this.destroy$))
            .subscribe({
                next: () => {
                    this.#notify.success(isCreate ? 'admin.components.success.created' : 'admin.components.success.updated');
                    this.loadItems();
                },
                error: () => this.#notify.alert('admin.components.errors.saveTranslationsFailed')
            });
    }

    deleteComponent(componentId: number): void {
        if (!confirm(this.#transloco.translate('admin.components.confirmDelete'))) {
            return;
        }

        this.#componentService.deleteComponent(componentId)
            .pipe(take(1), takeUntil(this.destroy$))
            .subscribe({
                next: () => {
                    this.#notify.success('admin.components.success.deleted');
                    this.loadItems();
                },
                error: () => this.#notify.alert('admin.components.errors.deleteFailed')
            });
    }

    openTypesManager(): void {
        const dialogRef = this.#matDialog.open(ComponentTypesManagerComponent, {
            width: '900px',
            height: '600px',
            disableClose: false
        });

        dialogRef.afterClosed().pipe(take(1)).subscribe(() => {
            this.#loadComponentTypes();
        });
    }
}
