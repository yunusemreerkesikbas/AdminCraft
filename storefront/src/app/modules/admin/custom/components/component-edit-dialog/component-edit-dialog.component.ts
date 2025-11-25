import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { TranslocoModule } from '@jsverse/transloco';
import { take, takeUntil } from 'rxjs';
import { ComponentDetailDto, UpdateComponentRequest, ComponentI18nRequest, ComponentStatus } from '../models/component-library.types';
import { ComponentLibraryService } from '../services/component-library.service';
import { ComponentEntryListComponent } from '../entries/component-entry-list/component-entry-list.component';
import { BaseI18nDialogComponent } from '@shared/components/base-i18n-dialog';
import { I18nDialogData } from '@shared/components/base-dialog';
import { SpaLinkRepeaterComponent } from '@shared/components/custom-ui/spa-link-repeater/spa-link-repeater.component';

interface DialogData extends I18nDialogData<ComponentDetailDto> {
    component: ComponentDetailDto;
}

@Component({
    selector: 'spa-component-edit-dialog',
    templateUrl: './component-edit-dialog.component.html',
    styleUrls: ['./component-edit-dialog.component.scss'],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatDialogModule,
        MatTabsModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatCheckboxModule,
        MatButtonModule,
        TranslocoModule,
        ComponentEntryListComponent,
        SpaLinkRepeaterComponent
    ]
})
export class ComponentEditDialogComponent extends BaseI18nDialogComponent<ComponentDetailDto, never, UpdateComponentRequest> {
    protected override service: any = inject(ComponentLibraryService);
    protected override readonly data!: DialogData;

    tabs = computed(() => ['general', ...this.languages, 'items']);

    protected buildGeneralForm(): FormGroup {
        return this.fb.group({
            name: [this.data.component.name, Validators.required],
            order: [this.data.component.baseData.order || 0, Validators.required],
            isVisible: [this.data.component.baseData.isVisible ?? true],
            styleClasses: [this.data.component.baseData.styleClasses || ''],
            status: [this.data.component.status || ComponentStatus.DRAFT]
        });
    }

    protected buildI18nForm(lang: string): FormGroup {
        const translation = this.data.component.translations?.[lang.toUpperCase()];
        return this.fb.group({
            title: [translation?.baseLocalizedData.title || ''],
            description: [translation?.baseLocalizedData.description || ''],
            subtitle: [translation?.baseLocalizedData.subtitle || ''],
            links: [translation?.baseLocalizedData.links || []]
        });
    }

    protected buildPayload(): never {
        throw new Error('Use tab-specific save methods instead');
    }

    saveGeneral(): void {
        if (this.generalForm.invalid) {
            this.notify.warning('admin.validation.generalFormInvalid');
            return;
        }

        const formValue = this.generalForm.value;
        const payload: UpdateComponentRequest = {
            name: formValue.name!,
            baseData: {
                order: formValue.order!,
                isVisible: formValue.isVisible!,
                styleClasses: formValue.styleClasses || undefined
            },
            status: formValue.status as ComponentStatus
        };

        this.setSubmitting(true);
        this.service.updateComponent(this.data.component.id, payload)
            .pipe(take(1), takeUntil(this.destroy$))
            .subscribe({
                next: () => {
                    this.setSubmitting(false);
                    this.notify.success('admin.components.success.updated');
                },
                error: () => {
                    this.setSubmitting(false);
                    this.notify.alert('admin.components.errors.updateFailed');
                }
            });
    }

    saveI18n(language: string): void {
        const form = this.i18nForms[language];
        if (form.invalid) {
            this.notify.warning('admin.validation.i18nFormInvalid');
            return;
        }

        const formValue = form.value;
        const payload: ComponentI18nRequest = {
            baseLocalizedData: {
                title: formValue.title || undefined,
                subtitle: formValue.subtitle || undefined,
                description: formValue.description || undefined,
                links: formValue.links || undefined
            },
            status: ComponentStatus.DRAFT
        };

        this.setSubmitting(true);
        this.service.updateComponentI18n(this.data.component.id, language, payload)
            .pipe(take(1), takeUntil(this.destroy$))
            .subscribe({
                next: () => {
                    this.setSubmitting(false);
                    this.notify.success('admin.components.success.translationSaved');
                },
                error: () => {
                    this.setSubmitting(false);
                    this.notify.alert('admin.components.errors.saveTranslationsFailed');
                }
            });
    }

    publishI18n(language: string): void {
        this.setSubmitting(true);
        this.service.publishComponentI18n(this.data.component.id, language)
            .pipe(take(1), takeUntil(this.destroy$))
            .subscribe({
                next: () => {
                    this.setSubmitting(false);
                    this.notify.success('admin.components.success.published');
                },
                error: () => {
                    this.setSubmitting(false);
                    this.notify.alert('admin.components.errors.publishFailed');
                }
            });
    }

    protected onSave(): void {
        this.close();
    }

    closeDialog(): void {
        this.close();
    }
}

