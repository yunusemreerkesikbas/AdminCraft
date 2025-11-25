import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { forkJoin, take } from 'rxjs';
import { FuseCardComponent } from '@fuse/components/card';
import { fuseAnimations } from '@fuse/animations';
import { ComponentEntry, CreateEntryRequest, EntryFieldDefinition, EntryI18nDto, EntryI18nRequest, UpdateEntryRequest } from '../../models/component-entry.types';
import { ComponentEntryService } from '../../services/component-entry.service';
import { EntryFieldService } from '../../services/entry-field.service';

interface DialogData {
    mode: 'create' | 'edit';
    componentId: number;
    componentTypeId?: number;
    languages?: string[];
    entryId?: number;
    entry?: ComponentEntry;
    translations?: Record<string, EntryI18nDto>;
}

@Component({
    selector: 'spa-component-entry-form',
    templateUrl: './component-entry-form.component.html',
    styleUrls: ['./component-entry-form.component.scss'],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    animations: fuseAnimations,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatDialogModule,
        MatTabsModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatSelectModule,
        MatCheckboxModule,
        MatButtonModule,
        TranslocoModule,
        FuseCardComponent
    ]
})
export class ComponentEntryFormComponent implements OnInit {
    #dialogRef = inject(MatDialogRef<ComponentEntryFormComponent>);
    #fb = inject(FormBuilder);
    #entryService = inject(ComponentEntryService);
    #fieldService = inject(EntryFieldService);
    #notify = inject(NotificationService);
    #transloco = inject(TranslocoService);
    data = inject<DialogData>(MAT_DIALOG_DATA);
    
    mode: 'create' | 'edit' = 'create';
    languages: string[] = [];
    activeTab = 0;

    generalForm!: FormGroup;
    i18nForms: Record<string, FormGroup> = {};
    
    fieldDefinitions = signal<EntryFieldDefinition[]>([]);
    isLoading = signal<boolean>(false);
    isSaving = signal<boolean>(false);
    
    canSave = computed(() => 
        this.generalForm.valid && 
        !this.isSaving() && 
        !this.isLoading()
    );

    statusOptions = [
        { value: 'DRAFT', labelKey: 'admin.common.status.draft' },
        { value: 'ACTIVE', labelKey: 'admin.common.status.active' },
        { value: 'INACTIVE', labelKey: 'admin.common.status.inactive' }
    ];

    ngOnInit(): void {
        this.mode = this.data.mode;
        this.languages = this.data.languages || ['tr', 'en'];

        this.#buildGeneralForm();
        this.#buildI18nForms(); // Always build i18n forms first

        if (this.data.componentTypeId) {
            this.#loadFieldDefinitions();
        }
    }

    #buildGeneralForm(): void {
        this.generalForm = this.#fb.group({
            sortOrder: [this.data.entry?.sortOrder ?? 0, Validators.required],
            isVisible: [this.data.entry?.isVisible ?? true],
            styleClasses: [this.data.entry?.styleClasses || ''],
            status: [this.data.entry?.status ?? 'DRAFT', Validators.required]
        });
    }

    #loadFieldDefinitions(): void {
        this.isLoading.set(true);
        this.#fieldService.getFields(this.data.componentTypeId)
            .pipe(take(1))
            .subscribe({
                next: (fields) => {
                    this.fieldDefinitions.set(fields);
                    this.#addDynamicFieldsToForms();
                    this.isLoading.set(false);
                },
                error: () => {
                    this.#notify.alert('admin.components.entries.loadFieldsFailed');
                    this.isLoading.set(false);
                }
            });
    }

    #buildI18nForms(): void {
        this.languages.forEach(lang => {
            const formGroup = this.#fb.group({
                title: [this.data.translations?.[lang]?.title || ''],
                description: [this.data.translations?.[lang]?.description || '']
            });

            this.i18nForms[lang] = formGroup;
        });
    }

    #addDynamicFieldsToForms(): void {
        this.languages.forEach(lang => {
            const formGroup = this.i18nForms[lang];
            if (!formGroup) return;

            this.fieldDefinitions().forEach(field => {
                const control = this.#createControlForFieldType(field, lang);
                (formGroup as any).addControl(field.fieldKey, control);
            });
        });
    }

    #createControlForFieldType(field: EntryFieldDefinition, language: string): FormControl {
        const validators = [];
        if (field.isRequired) validators.push(Validators.required);

        const initialValue = this.data.translations?.[language]?.[field.fieldKey] ?? this.#getDefaultValue(field.fieldType);

        switch (field.fieldType) {
            case 'text':
                if (field.maxLength) validators.push(Validators.maxLength(field.maxLength));
                break;
            case 'number':
                if (field.minValue !== undefined) validators.push(Validators.min(field.minValue));
                if (field.maxValue !== undefined) validators.push(Validators.max(field.maxValue));
                break;
        }

        return new FormControl(initialValue, validators);
    }

    #getDefaultValue(fieldType: string): any {
        switch (fieldType) {
            case 'boolean': return false;
            case 'number': return null;
            default: return '';
        }
    }

    protected getFieldLabel(fieldKey: string): string {
        return `admin.components.entryFields.custom.${fieldKey}`;
    }

    protected getFieldLabelWithFallback(fieldKey: string): string {
        const i18nKey = `admin.components.entryFields.custom.${fieldKey}`;
        const translation = this.#transloco.translate(i18nKey);
        
        if (translation !== i18nKey) {
            return translation;
        }
        
        return this.#humanizeFieldKey(fieldKey);
    }

    #humanizeFieldKey(fieldKey: string): string {
        return fieldKey
            .replace(/([A-Z])/g, ' $1')
            .replace(/^./, str => str.toUpperCase())
            .trim();
    }

    save(): void {
        if (this.generalForm.invalid) {
            this.#notify.warning('admin.validation.generalFormInvalid');
            return;
        }

        const hasInvalidI18n = Object.values(this.i18nForms).some(form => form.invalid);
        if (hasInvalidI18n) {
            this.#notify.warning('admin.validation.i18nFormsInvalid');
            return;
        }

        this.isSaving.set(true);

        if (this.data.mode === 'create') {
            this.#createEntry();
        } else {
            this.#updateEntry();
        }
    }

    #createEntry(): void {
        const payload: CreateEntryRequest = {
            componentId: this.data.componentId,
            sortOrder: this.generalForm.value.sortOrder,
            isVisible: this.generalForm.value.isVisible,
            styleClasses: this.generalForm.value.styleClasses || undefined
        };

        this.#entryService.create(payload)
            .pipe(take(1))
            .subscribe({
                next: (entry) => {
                    this.#saveI18nData(entry.id);
                },
                error: () => {
                    this.isSaving.set(false);
                    this.#notify.alert('admin.components.entries.createFailed');
                }
            });
    }

    #updateEntry(): void {
        const payload: UpdateEntryRequest = {
            sortOrder: this.generalForm.value.sortOrder,
            isVisible: this.generalForm.value.isVisible,
            styleClasses: this.generalForm.value.styleClasses || undefined,
            status: this.generalForm.value.status
        };

        this.#entryService.update(this.data.entry!.id, payload)
            .pipe(take(1))
            .subscribe({
                next: () => {
                    this.#saveI18nData(this.data.entry!.id);
                },
                error: () => {
                    this.isSaving.set(false);
                    this.#notify.alert('admin.components.entries.updateFailed');
                }
            });
    }

    #saveI18nData(entryId: number): void {
        const i18nRequests = this.languages.map(lang => {
            const formData = this.i18nForms[lang].value;
            const baseFields = ['title', 'description'];

            const dynamicFields: Record<string, any> = {};
            Object.keys(formData).forEach(key => {
                if (!baseFields.includes(key)) {
                    dynamicFields[key] = formData[key];
                }
            });

            const request: EntryI18nRequest = {
                title: formData.title || undefined,
                description: formData.description || undefined,
                ...dynamicFields
            };

            return this.#entryService.upsertI18n(entryId, lang, request);
        });

        forkJoin(i18nRequests)
            .pipe(take(1))
            .subscribe({
                next: () => {
                    this.isSaving.set(false);
                    this.#notify.success(
                        this.data.mode === 'create' 
                            ? 'admin.components.entries.createSuccess' 
                            : 'admin.components.entries.updateSuccess'
                    );
                    this.#dialogRef.close(true);
                },
                error: () => {
                    this.isSaving.set(false);
                    this.#notify.alert('admin.components.entries.saveI18nFailed');
                }
            });
    }

    cancel(): void {
        this.#dialogRef.close();
    }

    getFieldLabelKey(fieldKey: string): string {
        return `admin.components.entryFields.custom.${fieldKey}`;
    }
}

