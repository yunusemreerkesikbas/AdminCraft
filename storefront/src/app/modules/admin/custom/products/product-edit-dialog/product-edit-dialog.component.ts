import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
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
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from '@shared/components/spa-dialog';
import { SpaLocalizedFormDialog } from '@shared/components/spa-localized-form-dialog';
import { SpaTabContainerComponent, SpaTabContentDirective, TabDefinition } from '@shared/components/spa-tab-container';
import { NotificationService } from '@shared/notifications/notification.service';
import { forkJoin, map, take } from 'rxjs';
import { SpaMediaPickerComponent } from '../../media/components/spa-media-picker/spa-media-picker.component';
import { Category } from '../models/category.types';
import { AttributeDefinition, ProductType } from '../models/product-type.types';
import { ProductCompositeRequest, ProductI18nRequest, ProductMediaResponse } from '../models/product.types';
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
        SpaMediaPickerComponent
    ]
})
export class ProductEditDialogComponent extends SpaLocalizedFormDialog<boolean, ProductEditDialogData> implements OnInit {
    override data = inject<ProductEditDialogData>(MAT_DIALOG_DATA);
    
    #productService = inject(ProductService);
    #productTypeService = inject(ProductTypeService);
    #categoryService = inject(CategoryService);
    #dynamicFormService = inject(SpaDynamicFormService);
    #notificationService = inject(NotificationService);
    #languageContextService = inject(LanguageContextService);
    override languages = this.#languageContextService.supportedLanguages();
    productTypesSig = signal<ProductType[]>([]);
    categoriesSig = signal<Category[]>([]);
    attributeDefinitionsSig = signal<AttributeDefinition[]>([]);
    dynamicFieldsConfigSig = computed(() => this.#mapToDynamicConfig(this.attributeDefinitionsSig()));
    attributesForm: FormGroup;
    gallerySig = signal<ProductMediaResponse[]>([]);
    isLoadingSig = signal(false);
    statusOptions = [
        { value: 'DRAFT', label: 'DRAFT' },
        { value: 'PUBLISHED', label: 'PUBLISHED' }
    ];

    constructor() {
        super();
        this.attributesForm = new FormGroup({});
    }

    get tabs(): TabDefinition[] {
        const tabs: TabDefinition[] = [
            { id: 'general', label: 'admin.common.tabs.general', icon: 'settings' },
            ...this.languages.map(lang => ({
                id: 'lang-' + lang,
                label: lang.toUpperCase(),
                icon: 'translate'
            })),
            { id: 'attributes', label: 'admin.products.tabs.attributes', icon: 'list_alt', disabled: !this.generalForm?.get('productTypeId')?.value },
            { id: 'categories', label: 'admin.products.tabs.categories', icon: 'category' },
            { id: 'media', label: 'admin.products.tabs.media', icon: 'perm_media' }
        ];
        return tabs;
    }

    override ngOnInit(): void {
        super.ngOnInit();
        this.loadInitialData();
        this.generalForm.get('productTypeId')?.valueChanges.subscribe(typeId => {
            if (typeId) {
                this.loadAttributes(typeId);
            } else {
                this.attributeDefinitionsSig.set([]);
                Object.keys(this.attributesForm.controls).forEach(key => this.attributesForm.removeControl(key));
            }
        });
    }

    protected buildGeneralForm(): FormGroup {
        return this.fb.group({
            sku: ['', [Validators.required]],
            productTypeId: [null, [Validators.required]],
            basePrice: [0, [Validators.required, Validators.min(0)]],
            currency: ['TRY', [Validators.required]], // TODO: Make currency configurable
            status: ['DRAFT', [Validators.required]],
            isVisible: [true],
            responsiveMediaId: [null],
            categoryIds: [[]],
            primaryCategoryId: [null]
        });
    }

    protected buildI18nForm(lang: string): FormGroup {
        return this.fb.group({
            name: ['', Validators.required],
            shortDescription: [''],
            description: [''],
            seoTitle: [''],
            seoDescription: ['']
        });
    }

    loadInitialData(): void {
        this.isLoadingSig.set(true);
        const types$ = this.#productTypeService.listPaged({ page: 0, size: 100 }).pipe(
            map(page => page?.content || [])
        );
        const categories$ = this.#categoryService.getTree().pipe(map(tree => this.#flattenTree(tree)));
        const sources: any = {
            types: types$,
            categories: categories$
        };

        if (this.data.mode === 'edit' && this.data.productId) {
            sources.product = this.#productService.getComposite(this.data.productId);
        }

        forkJoin(sources).pipe(take(1)).subscribe({
            next: (res: any) => {
                this.productTypesSig.set(res.types || []);
                this.categoriesSig.set(res.categories || []);
                
                if (res.product) {
                    this.patchProductData(res.product);
                }
                
                this.isLoadingSig.set(false);
            },
            error: () => {
                this.#notificationService.alert('admin.common.errors.loadFailed');
                this.isLoadingSig.set(false);
            }
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
        this.generalForm.patchValue({
            sku: product.sku,
            productTypeId: product.productTypeId,
            basePrice: product.basePrice,
            currency: product.currency,
            status: product.status,
            isVisible: product.isVisible,
            responsiveMediaId: product.responsiveMedia?.id,
            categoryIds: product.categories?.map((c: any) => c.id) || [],
            primaryCategoryId: product.categories?.find((c: any) => c.isPrimary)?.id
        });

        const translations = product.translations || {};
        this.languages.forEach(lang => {
            if (translations[lang]) {
                this.i18nForms[lang].patchValue(translations[lang]);
            }
        });
        this.loadAttributes(product.productTypeId, product.attributes);
        if (product.gallery) {
            this.gallerySig.set(product.gallery);
        }
    }

    loadAttributes(typeId: number, existingAttributes?: any[]): void {
        this.#productTypeService.getAttributes(typeId).pipe(take(1)).subscribe(attrs => {
            this.attributeDefinitionsSig.set(attrs);
            this.#dynamicFormService.addControlsToFormGroup(this.attributesForm, this.dynamicFieldsConfigSig(), {});
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
        return attrs.map(attr => ({
            key: attr.code,
            label: attr.name,
            type: attr.fieldType.toLowerCase() as any,
            required: attr.isRequired,
        }));
    }

    save(): void {
        if (this.generalForm.invalid) {
            this.generalForm.markAllAsTouched();
            this.#notificationService.warning('admin.validation.generalFormInvalid');
            return;
        }
        
        if (this.attributesForm.invalid) {
            this.attributesForm.markAllAsTouched();
            this.#notificationService.warning('admin.validation.attributesFormInvalid');
            return;
        }

        this.setSubmitting(true);
        const formValue = this.generalForm.value;

        const translations: Record<string, ProductI18nRequest> = {};
        this.languages.forEach(lang => {
            translations[lang] = this.i18nForms[lang].value;
        });

        const request: ProductCompositeRequest = {
            sku: formValue.sku,
            productTypeId: formValue.productTypeId,
            basePrice: formValue.basePrice,
            currency: formValue.currency,
            status: formValue.status,
            isVisible: formValue.isVisible,
            responsiveMediaId: formValue.responsiveMediaId || undefined,
            translations,
            attributes: this.attributesForm.value,
            categoryIds: formValue.categoryIds || [],
            primaryCategoryId: formValue.primaryCategoryId,
            galleryMediaIds: this.gallerySig().map(m => m.mediaId)
        };

        let request$;
        if (this.data.mode === 'create') {
            request$ = this.#productService.create(request);
        } else {
            request$ = this.#productService.update(this.data.productId!, request);
        }

        request$.pipe(take(1)).subscribe({
            next: () => {
                this.setSubmitting(false);
                this.#notificationService.success('admin.common.messages.saveSuccess');
                this.close(true);
            },
            error: (err) => {
                this.setSubmitting(false);
                this.#notificationService.alert(err?.error?.message || 'admin.common.errors.saveFailed');
            }
        });
    }

    addGalleryImage(media: any): void {
        if (!media) return;
        const newMedia: ProductMediaResponse = {
            id: 0,
            mediaId: media.id,
            mediaType: 'GALLERY',
            sortOrder: this.gallerySig().length,
            media: media
        };
        
        this.gallerySig.update(current => [...current, newMedia]);
    }

    removeGalleryImage(index: number): void {
        this.gallerySig.update(current => current.filter((_, i) => i !== index));
    }
}
