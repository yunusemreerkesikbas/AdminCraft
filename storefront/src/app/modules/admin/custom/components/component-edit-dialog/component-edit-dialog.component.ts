import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    Inject,
    ViewEncapsulation,
    inject,
    signal,
} from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
    MAT_DIALOG_DATA,
    MatDialogModule,
    MatDialogRef,
} from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaCheckboxComponent } from 'app/shared/components/custom-ui/spa-checkbox/spa-checkbox.component';
import { SpaInputComponent } from 'app/shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from 'app/shared/components/custom-ui/spa-select/spa-select.component';
import { SpaTextareaComponent } from 'app/shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { ComponentEntryListComponent } from '../entries/component-entry-list/component-entry-list.component';
import { ComponentDetailDto, ComponentStatus, ComponentTypeDto } from '../models/component-library.types';

export interface ComponentEditDialogData {
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
        MatDialogModule,
        MatIconModule,
        MatTabsModule,
        TranslocoModule,
        SpaInputComponent,
        SpaSelectComponent,
        SpaCheckboxComponent,
        SpaTextareaComponent,
        ComponentEntryListComponent,
    ],
    templateUrl: './component-edit-dialog.component.html',
    styleUrls: ['./component-edit-dialog.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ComponentEditDialogComponent {
    #formBuilder = inject(FormBuilder);
    #dialogRef = inject(MatDialogRef<ComponentEditDialogComponent>);

    activeTab = 0;
    isSubmitting = signal(false);
    languages: string[] = [];
    
    generalForm: FormGroup;
    i18nForms: { [key: string]: FormGroup } = {};
    
    statusOptions = Object.values(ComponentStatus).map(s => ({ value: s, label: s }));

    constructor(
        @Inject(MAT_DIALOG_DATA) public data: ComponentEditDialogData
    ) {
        this.languages = data.languages || ['en', 'tr'];
        this.generalForm = this.#formBuilder.group({
            name: [data.component?.name || '', Validators.required],
            componentTypeId: [
                data.component?.componentTypeId || '',
                Validators.required,
            ],
            status: [data.component?.status, Validators.required],
            isVisible: [data.component?.isVisible ?? true],
            styleClasses: [data.component?.styleClasses || ''],
            displayOrder: [data.component?.displayOrder || 0]
        });

        this.data.languages.forEach((lang) => {
            const translation = data.component?.translations?.[lang];
            this.i18nForms[lang] = this.#formBuilder.group({
                title: [translation?.title || ''],
                subtitle: [translation?.subtitle || ''],
                description: [translation?.description || ''],
            });
        });
    }

    saveGeneral(): void {
        if (this.generalForm.invalid) return;

        this.isSubmitting.set(true);
        if (this.data.mode === 'create') {
             const result = {
                ...this.generalForm.value,
                ...this.#getI18nValues()
            };
            this.#dialogRef.close(result);
        } else {
             setTimeout(() => {
                this.isSubmitting.set(false);
                this.#dialogRef.close(true);
            }, 1000);
        }
    }
    
    #getI18nValues(): any {
        const values: any = {};
        this.data.languages.forEach(lang => {
            values[lang] = this.i18nForms[lang].value;
        });
        return values;
    }

    closeDialog(): void {
        this.#dialogRef.close();
    }
}
