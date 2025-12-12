import { CommonModule, UpperCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, ViewEncapsulation } from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SpaCheckboxComponent } from '@shared/components/custom-ui/spa-checkbox/spa-checkbox.component';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from '@shared/components/spa-dialog';
import { SpaLocalizedFormDialogData } from '@shared/components/spa-dialog-base/spa-dialog-base.types';
import { SpaLocalizedFormDialog } from '@shared/components/spa-localized-form-dialog';
import { ComponentEntryListComponent } from '../entries/component-entry-list/component-entry-list.component';
import { ComponentDetailDto, ComponentStatus, ComponentTypeDto } from '../models/component-library.types';

export interface ComponentEditDialogData extends SpaLocalizedFormDialogData<ComponentDetailDto> {
    mode: 'create' | 'edit';
    component?: ComponentDetailDto;
    componentTypes?: ComponentTypeDto[];
    languages: string[];
}

@Component({
    selector: 'spa-component-edit-dialog',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        MatTabsModule,
        TranslocoModule,
        UpperCasePipe,
        SpaInputComponent,
        SpaSelectComponent,
        SpaCheckboxComponent,
        SpaTextareaComponent,
        ComponentEntryListComponent,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent
    ],
    templateUrl: './component-edit-dialog.component.html',
    styleUrls: ['./component-edit-dialog.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ComponentEditDialogComponent extends SpaLocalizedFormDialog<any, ComponentEditDialogData> {
    readonly #translocoService = inject(TranslocoService);

    statusOptions = Object.values(ComponentStatus).map(s => ({ value: s, label: s }));
    componentTypeOptions: { value: any; label: string }[] = [];
    override languages: string[] = [];

    readonly dialogTitle = this.data?.component
        ? this.#translocoService.translate('admin.dialog.title.edit')
        : this.#translocoService.translate('admin.dialog.title.create');

    constructor() {
        super();
        this.languages = this.data?.languages || ['en', 'tr'];
        this.componentTypeOptions = (this.data?.componentTypes || []).map(type => ({
            value: type.id,
            label: type.name
        }));
    }

    protected buildGeneralForm(): FormGroup {
        const component = this.data?.component;
        return this.fb.group({
            name: [component?.name || '', Validators.required],
            componentTypeId: [component?.componentTypeId || '', Validators.required],
            status: [component?.status, Validators.required],
            isVisible: [component?.isVisible ?? true],
            styleClasses: [component?.styleClasses || ''],
            displayOrder: [component?.displayOrder || 0]
        });
    }

    protected buildI18nForm(lang: string): FormGroup {
        const translation = this.data?.component?.translations?.[lang];
        return this.fb.group({
            title: [translation?.title || ''],
            subtitle: [translation?.subtitle || ''],
            description: [translation?.description || '']
        });
    }

    save(): void {
        if (this.generalForm.invalid) return;

        this.setSubmitting(true);

        const result = {
            ...this.generalForm.value,
            ...this.buildI18nPayload()
        };

        this.close(result);
    }
}
