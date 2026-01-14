import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, OnInit } from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { LanguageContextService } from '@core/services/language-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaCheckboxComponent } from '@shared/components/custom-ui/spa-checkbox/spa-checkbox.component';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from '@shared/components/spa-dialog';
import { SpaLocalizedFormDialog } from '@shared/components/spa-localized-form-dialog';
import { SpaTabContainerComponent, SpaTabContentDirective, TabDefinition } from '@shared/components/spa-tab-container';
import { VALIDATION_LIMITS, VALIDATION_PATTERNS } from '@shared/constants/validation.constants';
import { NotificationService } from '@shared/notifications/notification.service';
import { take } from 'rxjs';
import { Category, CategoryCompositeRequest, CategoryI18nRequest } from '../../models/category.types';
import { CategoryService } from '../../services/category.service';

export interface CategoryEditDialogData {
    mode: 'create' | 'edit';
    item?: Category;
    parentId?: number;
    categories?: Category[];
}

@Component({
    selector: 'spa-category-edit-dialog',
    templateUrl: './category-edit-dialog.component.html',
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        TranslocoModule,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent,
        SpaInputComponent,
        SpaSelectComponent,
        SpaTextareaComponent,
        SpaCheckboxComponent,
        SpaTabContainerComponent,
        SpaTabContentDirective
    ]
})
export class CategoryEditDialogComponent extends SpaLocalizedFormDialog<boolean, CategoryEditDialogData> implements OnInit {
    override data = inject<CategoryEditDialogData>(MAT_DIALOG_DATA);
    #service = inject(CategoryService);
    #notificationService = inject(NotificationService);
    #languageContextService = inject(LanguageContextService);

    override languages = this.#languageContextService.supportedLanguages();
    parentOptions = computed(() => {
        return (this.data.categories || [])
            .filter(c => c.id !== this.data.item?.id)
            .map(c => ({ value: c.id, label: `${c.code} (${c.name || c.uid})` }));
    });

    tabs = computed<TabDefinition[]>(() => {
        return [
            { id: 'general', label: 'admin.common.general', icon: 'settings' },
            ...this.languages.map(lang => ({
                id: 'lang-' + lang,
                label: lang.toUpperCase(),
                icon: 'translate'
            }))
        ];
    });

    override ngOnInit(): void {
        super.ngOnInit();
        if (this.data.mode === 'edit' && this.data.item) {
            this.loadTranslations();
        }
    }

    protected buildGeneralForm(): FormGroup {
        return this.fb.group({
            code: [this.data.item?.code || '', [
                Validators.required,
                Validators.maxLength(VALIDATION_LIMITS.CATEGORY_CODE_MAX),
                Validators.pattern(VALIDATION_PATTERNS.CATEGORY_CODE)
            ]],
            parentId: [this.data.item?.parentId || this.data.parentId || null],
            sortOrder: [this.data.item?.sortOrder || 0],
            isVisible: [this.data.item?.isVisible ?? true]
        });
    }

    protected buildI18nForm(lang: string): FormGroup {
        return this.fb.group({
            name: ['', [
                Validators.required,
                Validators.maxLength(VALIDATION_LIMITS.CATEGORY_NAME_MAX)
            ]],
            description: ['']
        });
    }

    loadTranslations(): void {
        this.#service.getComposite(this.data.item!.id).pipe(take(1)).subscribe(composite => {
             const translations = (composite as any).translations || {};
             this.languages.forEach(lang => {
                 if (translations[lang]) {
                     this.i18nForms[lang].patchValue({
                         name: translations[lang].name,
                         description: translations[lang].description
                     });
                 }
             });
        });
    }

    save(): void {
        if (this.generalForm.invalid) {
            this.generalForm.markAllAsTouched();
            this.#notificationService.warning('admin.common.validation.generalFormInvalid');
            return;
        }
        
        const hasInvalidI18n = Object.values(this.i18nForms).some(f => f.invalid);
        if (hasInvalidI18n) {
            this.#notificationService.warning('admin.common.validation.i18nFormsInvalid');
            return;
        }

        this.setSubmitting(true);
        const formValue = this.generalForm.value;

        const translations: Record<string, CategoryI18nRequest> = {};
        this.languages.forEach(lang => {
            translations[lang] = this.i18nForms[lang].value;
        });

        const request: CategoryCompositeRequest = {
            code: formValue.code,
            parentId: formValue.parentId || undefined,
            sortOrder: formValue.sortOrder,
            isVisible: formValue.isVisible,
            translations
        };

        let request$;
        if (this.data.mode === 'create') {
            request$ = this.#service.create(request);
        } else {
            request$ = this.#service.update(this.data.item!.id, request);
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
}
