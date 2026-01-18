import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    computed,
    inject,
    OnInit,
    signal,
} from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LanguageContextService } from '@core/services/language-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaCheckboxComponent } from '@shared/components/custom-ui/spa-checkbox/spa-checkbox.component';
import { SpaDynamicFormComponent } from '@shared/components/custom-ui/spa-dynamic-form/spa-dynamic-form.component';
import { SpaDynamicFormService } from '@shared/components/custom-ui/spa-dynamic-form/spa-dynamic-form.service';
import { DynamicFieldConfig } from '@shared/components/custom-ui/spa-dynamic-form/spa-dynamic-form.types';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import {
    SpaDialogContentComponent,
    SpaDialogFooterComponent,
    SpaDialogHeaderComponent,
} from '@shared/components/spa-dialog';
import { SpaLocalizedFormDialog } from '@shared/components/spa-localized-form-dialog';
import {
    SpaTabContainerComponent,
    SpaTabContentDirective,
    TabDefinition,
} from '@shared/components/spa-tab-container';
import {
    VALIDATION_LIMITS,
    VALIDATION_NUMERIC,
    VALIDATION_PATTERNS,
} from '@shared/constants/validation.constants';
import { NotificationService } from '@shared/notifications/notification.service';
import {
    forkJoin,
    map,
    Observable,
    of,
    switchMap,
    take,
    takeUntil,
} from 'rxjs';
import { SpaMediaPickerComponent } from '../../media/components/spa-media-picker/spa-media-picker.component';
import { MediaService } from '../../media/media.service';
import { Category } from '../models/category.types';
import { AttributeDefinition, ProductType } from '../models/product-type.types';
import {
    ProductCompositeRequest,
    ProductI18nRequest,
} from '../models/product.types';
import { CategoryService } from '../services/category.service';
import { ProductTypeService } from '../services/product-type.service';
import { ProductService } from '../services/product.service';

export interface ProductEditDialogData {
    mode: 'create' | 'edit';
    productId?: number;
}

@Component({
    selector: 'spa-product-edit-dialog',
    templateUrl: './product-edit-dialog.component.html',
    styleUrls: ['./product-edit-dialog.component.scss'],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        TranslocoModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent,
        SpaInputComponent,
        SpaSelectComponent,
        SpaTextareaComponent,
        SpaCheckboxComponent,
        SpaTabContainerComponent,
        SpaTabContentDirective,
        SpaDynamicFormComponent,
        SpaMediaPickerComponent,
    ],
})
export class ProductEditDialogComponent
    extends SpaLocalizedFormDialog<boolean, ProductEditDialogData>
    implements OnInit
{
    override data = inject<ProductEditDialogData>(MAT_DIALOG_DATA);

    #productService = inject(ProductService);
    #productTypeService = inject(ProductTypeService);
    #categoryService = inject(CategoryService);
    #dynamicFormService = inject(SpaDynamicFormService);
    #notificationService = inject(NotificationService);
    #languageContextService = inject(LanguageContextService);
    #mediaService = inject(MediaService);
    override languages = this.#languageContextService.supportedLanguages();
    productTypes = signal<ProductType[]>([]);
    categories = signal<Category[]>([]);
    #attributeDefinitions = signal<AttributeDefinition[]>([]);
    dynamicFieldsConfig = computed(() =>
        this.#mapToDynamicConfig(this.#attributeDefinitions())
    );
    attributesForm: FormGroup;
    isLoadingSig = signal(false);
    #currentResponsiveMediaId: number | undefined;
    statusOptions = [
        { value: 'DRAFT', label: 'DRAFT' },
        { value: 'PUBLISHED', label: 'PUBLISHED' },
    ];

    constructor() {
        super();
        this.attributesForm = new FormGroup({});
    }

    get tabs(): TabDefinition[] {
        const tabs: TabDefinition[] = [
            { id: 'general', label: 'admin.common.general', icon: 'settings' },
            ...this.languages.map((lang) => ({
                id: 'lang-' + lang,
                label: lang.toUpperCase(),
                icon: 'translate',
            })),
            {
                id: 'attributes',
                label: 'admin.products.tabs.attributes',
                icon: 'list_alt',
                disabled: !this.generalForm?.get('productTypeId')?.value,
            },
            {
                id: 'categories',
                label: 'admin.products.tabs.categories',
                icon: 'category',
            },
            {
                id: 'media',
                label: 'admin.products.tabs.media',
                icon: 'perm_media',
            },
        ];
        return tabs;
    }

    override ngOnInit(): void {
        super.ngOnInit();
        setTimeout(() => {
            this.loadInitialData();
        });

        this.generalForm
            .get('productTypeId')
            ?.valueChanges.pipe(takeUntil(this.destroy$))
            .subscribe((typeId) => {
                if (typeId) {
                    this.loadAttributes(typeId);
                } else {
                    this.#attributeDefinitions.set([]);
                    Object.keys(this.attributesForm.controls).forEach((key) =>
                        this.attributesForm.removeControl(key)
                    );
                }
            });
    }

    protected buildGeneralForm(): FormGroup {
        return this.fb.group({
            sku: [
                '',
                [
                    Validators.required,
                    Validators.maxLength(VALIDATION_LIMITS.SKU_MAX),
                    Validators.pattern(VALIDATION_PATTERNS.SKU),
                ],
            ],
            productTypeId: [null, [Validators.required]],
            basePrice: [
                0,
                [
                    Validators.required,
                    Validators.min(VALIDATION_NUMERIC.PRICE_MIN),
                ],
            ],
            currency: [
                'TRY',
                [
                    Validators.required,
                    Validators.maxLength(VALIDATION_LIMITS.CURRENCY_MAX),
                ],
            ],
            status: ['DRAFT', [Validators.required]],
            isVisible: [true],
            responsiveMedia: [null],
            categoryIds: [[]],
            primaryCategoryId: [null],
            galleryImages: [[]],
        });
    }

    protected buildI18nForm(lang: string): FormGroup {
        return this.fb.group({
            name: ['', [Validators.maxLength(VALIDATION_LIMITS.NAME_MAX)]],
            shortDescription: [
                '',
                [Validators.maxLength(VALIDATION_LIMITS.SHORT_DESCRIPTION_MAX)],
            ],
            description: [''],
            seoTitle: [
                '',
                [Validators.maxLength(VALIDATION_LIMITS.SEO_TITLE_MAX)],
            ],
            seoDescription: [
                '',
                [Validators.maxLength(VALIDATION_LIMITS.SEO_DESCRIPTION_MAX)],
            ],
        });
    }

    loadInitialData(): void {
        this.isLoadingSig.set(true);
        const types$ = this.#productTypeService
            .listPaged({ page: 0, size: 100 })
            .pipe(map((page) => page?.content || []));
        const categories$ = this.#categoryService
            .getTree()
            .pipe(map((tree) => this.#flattenTree(tree)));
        const sources: any = {
            types: types$,
            categories: categories$,
        };

        if (this.data.mode === 'edit' && this.data.productId) {
            sources.product = this.#productService.getComposite(
                this.data.productId
            );
        }

        forkJoin(sources)
            .pipe(take(1))
            .subscribe({
                next: (res: any) => {
                    this.productTypes.set(res.types || []);
                    this.categories.set(res.categories || []);

                    if (res.product) {
                        this.patchProductData(res.product);
                    }

                    this.isLoadingSig.set(false);
                },
                error: () => {
                    this.#notificationService.alert(
                        'admin.common.errors.loadFailed'
                    );
                    this.isLoadingSig.set(false);
                },
            });
    }

    #flattenTree(tree: any[]): Category[] {
        const result: Category[] = [];
        const stack = [...tree];
        while (stack.length) {
            const node = stack.pop()!;
            result.push(node);
            if (node.children) {
                stack.push(...node.children);
            }
        }
        return result;
    }

    patchProductData(product: any): void {
        const responsiveMedia = product.images
            ? {
                  desktop: product.images.desktop,
                  mobile: product.images.mobile,
              }
            : null;

        this.#currentResponsiveMediaId = product.images?.id;

        this.generalForm.patchValue(
            {
                sku: product.sku,
                productTypeId: product.productTypeId,
                basePrice: product.basePrice,
                currency: product.currency,
                status: product.status,
                isVisible: product.isVisible,
                responsiveMedia: responsiveMedia,
                categoryIds: product.categories?.map((c: any) => c.id) || [],
                primaryCategoryId: product.categories?.find(
                    (c: any) => c.isPrimary
                )?.id,
            },
            { emitEvent: false }
        );

        const translations = product.translations || {};
        this.languages.forEach((lang) => {
            if (translations[lang]) {
                this.i18nForms[lang].patchValue(translations[lang]);
            }
        });
        this.loadAttributes(product.productTypeId, product.attributes);
        // Patch Gallery Images
        if (product.galleryImages && product.galleryImages.length > 0) {
            // The picker expects { desktop: Media, mobile: Media } objects for responsive mode
            const galleryMedia = product.galleryImages
                .filter((img: any) => img.media)
                .map((img: any) => ({
                    desktop: img.media.desktop,
                    mobile: img.media.mobile,
                }));

            this.generalForm.patchValue({
                galleryImages: galleryMedia,
            });
        }
    }

    loadAttributes(typeId: number, existingAttributes?: any[]): void {
        this.#productTypeService
            .getAttributes(typeId)
            .pipe(take(1))
            .subscribe((attrs) => {
                this.#attributeDefinitions.set(attrs);
                this.#dynamicFormService.addControlsToFormGroup(
                    this.attributesForm,
                    this.dynamicFieldsConfig(),
                    {}
                );
                if (existingAttributes) {
                    const values: any = {};
                    existingAttributes.forEach((attr: any) => {
                        values[attr.code] = attr.value;
                    });
                    this.attributesForm.patchValue(values);
                }
            });
    }

    #mapToDynamicConfig(attrs: AttributeDefinition[]): DynamicFieldConfig[] {
        return attrs.map((attr) => ({
            key: attr.code,
            label: attr.name,
            type: attr.fieldType.toLowerCase() as any,
            required: false,
        }));
    }

    save(): void {
        this.generalForm.markAllAsTouched();
        Object.values(this.i18nForms).forEach((f) => f.markAllAsTouched());

        if (this.generalForm.invalid) {
            this.#notificationService.warning(
                'admin.common.validation.generalFormInvalid'
            );
            return;
        }

        const hasAtLeastOneName = Object.values(this.i18nForms).some((f) => {
            const nameValue = f.get('name')?.value;
            return (
                nameValue &&
                typeof nameValue === 'string' &&
                nameValue.trim().length > 0
            );
        });
        if (!hasAtLeastOneName) {
            this.#notificationService.warning(
                'admin.common.validation.atLeastOneLanguageRequired'
            );
            return;
        }

        const hasInvalidI18nForms = Object.values(this.i18nForms).some((f) => {
            return this.formHasContent(f) && f.invalid;
        });
        if (hasInvalidI18nForms) {
            this.#notificationService.warning(
                'admin.common.validation.i18nFormsInvalid'
            );
            return;
        }

        this.setSubmitting(true);
        const formValue = this.generalForm.value;

        const translations: Record<string, ProductI18nRequest> = {};
        this.languages.forEach((lang) => {
            translations[lang] = this.i18nForms[lang].value;
        });

        this.#handleResponsiveMedia()
            .pipe(
                switchMap((responsiveMediaId) => {
                    const request: ProductCompositeRequest = {
                        sku: formValue.sku,
                        productTypeId: formValue.productTypeId,
                        basePrice: formValue.basePrice,
                        currency: formValue.currency,
                        status: formValue.status,
                        isVisible: formValue.isVisible,
                        responsiveMediaId: responsiveMediaId || undefined,
                        translations,
                        attributes: this.attributesForm.value,
                        categoryIds: formValue.categoryIds || [],
                        primaryCategoryId: formValue.primaryCategoryId,
                        gallery:
                            formValue.galleryImages?.map((m: any) => ({
                                desktopMediaId: m.desktop?.id,
                                mobileMediaId: m.mobile?.id,
                            })) || [],
                    };

                    if (this.data.mode === 'create') {
                        return this.#productService.create(request);
                    } else {
                        return this.#productService.update(
                            this.data.productId!,
                            request
                        );
                    }
                }),
                take(1)
            )
            .subscribe({
                next: () => {
                    this.setSubmitting(false);
                    this.#notificationService.success(
                        'admin.common.messages.saveSuccess'
                    );
                    this.close(true);
                },
                error: (err) => {
                    this.setSubmitting(false);
                    this.#notificationService.alert(
                        err?.error?.message || 'admin.common.errors.saveFailed'
                    );
                },
            });
    }

    #handleResponsiveMedia(): Observable<number | null> {
        const responsiveValue = this.generalForm.get('responsiveMedia')?.value;
        const currentSetId = this.#currentResponsiveMediaId;

        const desktopMediaId =
            typeof responsiveValue?.desktop === 'number'
                ? responsiveValue.desktop
                : responsiveValue?.desktop?.id;
        const mobileMediaId =
            typeof responsiveValue?.mobile === 'number'
                ? responsiveValue.mobile
                : responsiveValue?.mobile?.id;

        if (!desktopMediaId && !mobileMediaId) {
            return of(null);
        }

        const responsiveMediaRequest = {
            desktopMediaId: desktopMediaId,
            mobileMediaId: mobileMediaId,
            code: `responsive_product_${Date.now()}`,
        };

        if (currentSetId) {
            return this.#mediaService
                .updateResponsiveMedia(currentSetId, responsiveMediaRequest)
                .pipe(map((response) => response.id));
        } else {
            return this.#mediaService
                .createResponsiveMedia(responsiveMediaRequest)
                .pipe(map((response) => response.id));
        }
    }
}
