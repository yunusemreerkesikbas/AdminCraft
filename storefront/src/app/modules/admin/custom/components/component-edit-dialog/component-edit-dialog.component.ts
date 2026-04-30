import { SpaLocalizedFormDialogData } from '@/app/shared/components/spa-dialog-base';
import { UpperCasePipe } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    inject,
    Injector,
    runInInjectionContext,
    signal,
    Signal,
    ViewEncapsulation,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SpaCheckboxComponent } from '@shared/components/custom-ui/spa-checkbox/spa-checkbox.component';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaDialogComponent } from '@shared/components/spa-dialog';
import { SpaLocalizedFormDialog } from '@shared/components/spa-localized-form-dialog';
import {
    SpaTabContainerComponent,
    SpaTabContentDirective,
    TabDefinition,
} from '@shared/components/spa-tab-container';
import {
    VALIDATION_LIMITS,
    VALIDATION_PATTERNS,
} from '@shared/constants/validation.constants';
import { NotificationService } from '@shared/notifications/notification.service';
import { Language } from '@shared/types/common.types';
import { map, Observable, of, startWith, switchMap, take } from 'rxjs';
import { SpaMediaPickerComponent } from '../../media/components/spa-media-picker/spa-media-picker.component';
import { MediaService } from '../../media/media.service';
import { NavigationNodeService } from '../../navigation/navigation-node.service';
import { NavigationNode } from '../../navigation/navigation-node.types';
import { ComponentEntryListComponent } from '../entries/component-entry-list/component-entry-list.component';
import {
    ComponentCompositeResponse,
    ComponentDetailDto,
    ComponentI18nRequest,
    ComponentTypeDto,
    CreateComponentCompositeRequest,
    NavigationType,
    UpdateComponentCompositeRequest,
} from '../models/component-library.types';
import { ComponentLibraryService } from '../services/component-library.service';

export interface ComponentEditDialogData
    extends SpaLocalizedFormDialogData<ComponentDetailDto> {
    mode: 'create' | 'edit';
    component?: ComponentDetailDto;
    componentTypes?: ComponentTypeDto[];
    languages: string[];
}

interface SelectOption<T = number> {
    value: T;
    label: string;
}

@Component({
    selector: 'spa-component-edit-dialog',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        UpperCasePipe,
        SpaInputComponent,
        SpaSelectComponent,
        SpaCheckboxComponent,
        SpaTextareaComponent,
        ComponentEntryListComponent,
        SpaDialogComponent,
        SpaMediaPickerComponent,
        SpaTabContainerComponent,
        SpaTabContentDirective,
    ],
    templateUrl: './component-edit-dialog.component.html',
    styleUrls: ['./component-edit-dialog.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ComponentEditDialogComponent extends SpaLocalizedFormDialog<
    ComponentCompositeResponse,
    ComponentEditDialogData
> {
    static readonly DEFAULT_NAVIGATION_TYPE = NavigationType.MAINMENU;
    static readonly NAVIGATION_NODE_PAGE_SIZE = 100;

    readonly #injector = inject(Injector);
    readonly #translocoService = inject(TranslocoService);
    readonly #componentService = inject(ComponentLibraryService);
    readonly #mediaService = inject(MediaService);
    readonly #navigationNodeService = inject(NavigationNodeService);
    readonly #notify = inject(NotificationService);
    readonly #tenantContext = inject(TenantContextService);

    protected componentTypeOptions: SelectOption[] = [];
    protected navigationTypeOptions: SelectOption<NavigationType>[] = [
        { value: NavigationType.MAINMENU, label: 'MAINMENU' },
        { value: NavigationType.STATICPAGE, label: 'STATICPAGE' },
    ];
    protected navigationNodeOptions: SelectOption[] = [];
    override languages: string[] = [];
    protected componentTypesById = new Map<number, ComponentTypeDto>();

    protected readonly dialogTitle = this.data?.component
        ? this.#translocoService.translate('admin.dialog.title.edit')
        : this.#translocoService.translate('admin.dialog.title.create');

    protected isNavigationAwareSig!: Signal<boolean>;

    constructor() {
        super();
        const types = this.data?.componentTypes || [];
        this.componentTypesById = new Map(types.map((type) => [type.id, type]));
        this.componentTypeOptions = types.map((type) => ({
            value: type.id,
            label: type.uid,
        }));
    }

    protected get tabs(): TabDefinition[] {
        const baseTabs: TabDefinition[] = [
            {
                id: 'general',
                label: 'admin.components.entries.tabs.general',
                icon: 'settings',
            },
            { id: 'media', label: 'admin.media.title', icon: 'image' },
            ...this.languages.map((lang) => ({
                id: 'lang-' + lang,
                label: lang.toUpperCase(),
                icon: 'translate',
            })),
        ];

        if (this.data?.component) {
            baseTabs.push({
                id: 'entries',
                label: 'admin.components.entries.tabs.items',
                icon: 'list',
            });
        }

        return baseTabs;
    }

    override ngOnInit(): void {
        const tenant = this.#tenantContext.tenant();
        if (this.data?.languages?.length) {
            this.languages = this.data.languages;
        } else if (tenant?.supportedLanguages) {
            this.languages = tenant.supportedLanguages.map((l) => l.code);
        } else {
            this.languages = ['en'];
        }
        super.ngOnInit();

        if (this.data?.mode === 'edit') {
            const component = this.data.component;
            const isNavAware = !!(
                component?.navigationType || component?.navigationNodeId
            );
            this.isNavigationAwareSig = signal(isNavAware);
            if (isNavAware) {
                this.#loadNavigationNodes();
            }
        } else {
            const componentTypeId = this.generalForm.get('componentTypeId')!;
            this.isNavigationAwareSig = runInInjectionContext(
                this.#injector,
                () =>
                    toSignal(
                        componentTypeId.valueChanges.pipe(
                            startWith(componentTypeId.value),
                            map((value) => {
                                const id = this.#toNullableNumber(value);
                                return (
                                    (id !== null
                                        ? this.componentTypesById.get(id)
                                        : undefined
                                    )?.navigationAware ?? false
                                );
                            })
                        ),
                        { initialValue: false }
                    )
            );
            this.#loadNavigationNodes();
        }
    }

    protected buildGeneralForm(): FormGroup {
        const component = this.data?.component;
        const responsiveMedia = component?.responsiveMedia;
        const responsiveValue = responsiveMedia
            ? {
                  desktopMedia: responsiveMedia.desktopMedia,
                  mobileMedia: responsiveMedia.mobileMedia,
              }
            : null;

        const isEdit = this.data?.mode === 'edit';

        return this.fb.group({
            uid: [
                component?.uid || '',
                [
                    Validators.required,
                    Validators.maxLength(VALIDATION_LIMITS.UID_TEMPLATE_MAX),
                    Validators.pattern(VALIDATION_PATTERNS.UID),
                ],
            ],
            componentTypeId: [
                { value: component?.componentTypeId || '', disabled: isEdit },
                Validators.required,
            ],
            componentTypeName: [
                { value: component?.componentTypeName || '', disabled: true },
            ],
            isVisible: [component?.isVisible ?? true],
            styleClasses: [
                component?.styleClasses || '',
                [
                    Validators.maxLength(
                        VALIDATION_LIMITS.COMPONENT_STYLE_CLASSES_MAX
                    ),
                ],
            ],
            displayOrder: [component?.displayOrder || 0],
            responsiveMedia: [responsiveValue],
            navigationNodeId: [component?.navigationNodeId ?? null],
            navigationType: [
                component?.navigationType ||
                    ComponentEditDialogComponent.DEFAULT_NAVIGATION_TYPE,
            ],
            searchBox: [component?.searchBox ?? false],
        });
    }

    protected buildI18nForm(lang: string): FormGroup {
        const translation = this.#getExistingTranslation(lang);
        return this.fb.group({
            title: [translation?.title || ''],
            subtitle: [translation?.subtitle || ''],
            description: [translation?.description || ''],
        });
    }

    save(): void {
        if (this.generalForm.invalid) return;

        const translations = this.#buildTranslations();

        this.setSubmitting(true);

        if (this.data.mode === 'create') {
            this.#createComponentComposite(translations);
        } else {
            this.#updateComponentComposite(translations);
        }
    }

    #createComponentComposite(
        translations: Record<Language, ComponentI18nRequest>
    ): void {
        const generalData = this.generalForm.value;
        const uid = (generalData.uid as string)?.trim();

        const request: CreateComponentCompositeRequest = {
            componentTypeId: generalData.componentTypeId,
            uid: uid,
            name: uid,
            displayOrder: generalData.displayOrder,
            isVisible: generalData.isVisible,
            styleClasses: generalData.styleClasses,
            translations: translations,
            ...this.#buildNavigationPayload(generalData),
        };

        this.#componentService
            .createCompositeWithResponse(request)
            .pipe(
                switchMap((response) =>
                    this.#handleResponsiveMedia(response.data.id).pipe(
                        map(() => response)
                    )
                ),
                take(1)
            )
            .subscribe({
                next: (response) => {
                    this.setSubmitting(false);
                    this.#notify.success(response.message ?? '');
                    this.close(response.data);
                },
                error: (error) => {
                    this.setSubmitting(false);
                    this.#notify.alert(error?.error?.message ?? '');
                },
            });
    }

    #updateComponentComposite(
        translations: Record<Language, ComponentI18nRequest>
    ): void {
        if (!this.data.component?.id) return;
        const componentId = this.data.component.id;
        const generalData = this.generalForm.value;
        const uid = (generalData.uid as string)?.trim();

        this.#resolveResponsiveMediaIdForComposite(componentId)
            .pipe(
                take(1),
                switchMap(({ responsiveMediaId, clearAfter }) => {
                    const request: UpdateComponentCompositeRequest = {
                        uid: uid,
                        name: uid,
                        displayOrder: generalData.displayOrder,
                        isVisible: generalData.isVisible,
                        styleClasses: generalData.styleClasses,
                        responsiveMediaId: responsiveMediaId ?? undefined,
                        translations: translations,
                        ...this.#buildNavigationPayload(generalData),
                    };
                    return this.#componentService
                        .updateCompositeWithResponse(componentId, request)
                        .pipe(
                            switchMap((response) =>
                                clearAfter
                                    ? this.#componentService
                                          .assignResponsiveMedia(
                                              componentId,
                                              null
                                          )
                                          .pipe(map(() => response))
                                    : of(response)
                            )
                        );
                }),
                take(1)
            )
            .subscribe({
                next: (response) => {
                    this.setSubmitting(false);
                    this.#notify.success(response.message ?? '');
                    this.close(response.data);
                },
                error: (error) => {
                    this.setSubmitting(false);
                    this.#notify.alert(error?.error?.message ?? '');
                },
            });
    }

    #buildTranslations(): Record<Language, ComponentI18nRequest> {
        const translations = {} as Record<Language, ComponentI18nRequest>;
        const isEditMode = this.data.mode === 'edit';
        let hasAnyTranslationContent = false;

        this.languages.forEach((lang) => {
            const form = this.i18nForms[lang];
            if (!form) return;

            const titleValue = this.#normalizeTranslationField(
                form.value.title as string
            );
            const subtitleValue = this.#normalizeTranslationField(
                form.value.subtitle as string
            );
            const descriptionValue = this.#normalizeTranslationField(
                form.value.description as string
            );
            const hasCurrentTranslationContent =
                titleValue.length > 0 ||
                subtitleValue.length > 0 ||
                descriptionValue.length > 0;

            if (hasCurrentTranslationContent) {
                hasAnyTranslationContent = true;
            }

            const existingTranslation = this.#getExistingTranslation(lang);
            const shouldIncludeLocale = isEditMode
                ? !!existingTranslation || hasCurrentTranslationContent
                : hasCurrentTranslationContent;

            if (!shouldIncludeLocale) {
                return;
            }

            translations[lang.toUpperCase() as Language] = {
                title: titleValue,
                subtitle: subtitleValue,
                description: descriptionValue,
            };
        });

        return Object.keys(translations).length > 0
            ? translations
            : ({} as Record<Language, ComponentI18nRequest>);
    }

    #normalizeTranslationField(value: string | null | undefined): string {
        return value?.trim() ?? '';
    }

    #getExistingTranslation(lang: string) {
        const key = lang.toUpperCase();
        return this.data?.component?.translations?.[key];
    }

    #resolveResponsiveMediaIdForComposite(
        componentId: number
    ): Observable<{ responsiveMediaId?: number; clearAfter: boolean }> {
        const raw = this.generalForm.getRawValue?.() ?? this.generalForm.value;
        const responsiveValue =
            raw?.responsiveMedia ??
            this.generalForm.get('responsiveMedia')?.value;
        const currentSetId = this.data.component?.responsiveMedia?.id;

        const desktopMediaId = this.#extractMediaId(
            responsiveValue?.desktopMedia
        );
        const mobileMediaId = this.#extractMediaId(
            responsiveValue?.mobileMedia
        );

        if (!desktopMediaId && !mobileMediaId) {
            return of({
                responsiveMediaId: undefined,
                clearAfter: !!currentSetId,
            });
        }

        const desktopId = desktopMediaId ?? mobileMediaId;
        const mobileId = mobileMediaId ?? desktopMediaId;
        if (!desktopId) {
            return of({
                responsiveMediaId: undefined,
                clearAfter: !!currentSetId,
            });
        }
        const responsiveMediaRequest = {
            desktopMediaId: desktopId,
            mobileMediaId: mobileId ?? desktopId,
        };

        if (currentSetId) {
            return this.#mediaService
                .updateResponsiveMedia(currentSetId, responsiveMediaRequest)
                .pipe(
                    map(() => ({
                        responsiveMediaId: currentSetId,
                        clearAfter: false,
                    }))
                );
        }
        return this.#mediaService
            .createResponsiveMedia(responsiveMediaRequest)
            .pipe(
                map((set) => ({ responsiveMediaId: set.id, clearAfter: false }))
            );
    }

    #extractMediaId(media: unknown): number | undefined {
        if (media == null) return undefined;
        if (typeof media === 'number' && !Number.isNaN(media)) return media;
        if (typeof media === 'object' && media !== null && 'id' in media) {
            const id = (media as { id: number }).id;
            return typeof id === 'number' && !Number.isNaN(id) ? id : undefined;
        }
        return undefined;
    }

    #handleResponsiveMedia(componentId: number): Observable<unknown> {
        return this.#resolveResponsiveMediaIdForComposite(componentId).pipe(
            switchMap(({ responsiveMediaId, clearAfter }) => {
                if (clearAfter) {
                    return this.#componentService.assignResponsiveMedia(
                        componentId,
                        null
                    );
                }
                if (responsiveMediaId != null) {
                    return this.#componentService.assignResponsiveMedia(
                        componentId,
                        responsiveMediaId
                    );
                }
                return of(null);
            })
        );
    }

    #loadNavigationNodes(): void {
        this.#navigationNodeService
            .listPaged({
                page: 0,
                size: ComponentEditDialogComponent.NAVIGATION_NODE_PAGE_SIZE,
                sort: 'createdAt,asc',
            })
            .pipe(take(1))
            .subscribe({
                next: (page) => {
                    const nodes = page?.content || [];
                    this.navigationNodeOptions =
                        this.#mapNavigationNodeOptions(nodes);
                },
                error: () => {
                    this.navigationNodeOptions = [];
                },
            });
    }

    #mapNavigationNodeOptions(nodes: NavigationNode[]): SelectOption[] {
        return nodes.map((node) => ({
            value: node.id,
            label: `${node.title || node.uid} (${node.uid})`,
        }));
    }

    #buildNavigationPayload(
        generalData: Record<string, unknown>
    ): Pick<
        CreateComponentCompositeRequest,
        'navigationNodeId' | 'navigationType' | 'searchBox'
    > {
        if (!this.isNavigationAwareSig()) {
            return {
                navigationNodeId: undefined,
                navigationType: undefined,
                searchBox: undefined,
            };
        }

        return {
            navigationNodeId:
                this.#toNullableNumber(generalData['navigationNodeId']) ??
                undefined,
            navigationType:
                (generalData['navigationType'] as NavigationType | undefined) ||
                ComponentEditDialogComponent.DEFAULT_NAVIGATION_TYPE,
            searchBox: !!generalData['searchBox'],
        };
    }

    #toNullableNumber(value: unknown): number | null {
        if (value === null || value === undefined || value === '') {
            return null;
        }
        const parsed = Number(value);
        return Number.isFinite(parsed) ? parsed : null;
    }
}
