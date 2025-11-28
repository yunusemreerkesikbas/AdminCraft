import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { TranslocoModule } from '@jsverse/transloco';
import { BaseDialogComponent } from '@shared/components/base-dialog';
import { NotificationService } from '@shared/notifications/notification.service';
import { take, takeUntil } from 'rxjs';
import { CreateEntryFieldRequest, ImportResultResponse } from '../../models/component-entry.types';
import { EntryFieldService } from '../../services/entry-field.service';

import { BaseDialogData } from '@shared/components/base-dialog';

interface DialogData extends BaseDialogData {
    componentTypeId: number;
}

@Component({
    selector: 'spa-entry-field-import-dialog',
    templateUrl: './entry-field-import-dialog.component.html',
    styleUrls: ['./entry-field-import-dialog.component.scss'],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatDialogModule,
        MatFormFieldModule,
        MatInputModule,
        MatTableModule,
        MatButtonModule,
        TranslocoModule
    ]
})
export class EntryFieldImportDialogComponent extends BaseDialogComponent<boolean, DialogData> {
    #fb = inject(FormBuilder);
    #service = inject(EntryFieldService);
    #notify = inject(NotificationService);

    importForm = this.#fb.group({
        jsonInput: ['', Validators.required]
    });

    parsedFields = signal<CreateEntryFieldRequest[]>([]);
    validationErrors = signal<string[]>([]);
    importResult = signal<ImportResultResponse | null>(null);

    displayedColumns = ['fieldKey', 'fieldType', 'isRequired'];

    parseJson(): void {
        const jsonInput = this.importForm.get('jsonInput')?.value;
        if (!jsonInput) {
            this.validationErrors.set(['JSON input is required']);
            return;
        }

        try {
            const parsed = JSON.parse(jsonInput);
            const errors: string[] = [];

            if (!Array.isArray(parsed.fields)) {
                errors.push('JSON must contain a "fields" array');
                this.validationErrors.set(errors);
                return;
            }

            if (parsed.fields.length === 0) {
                errors.push('Fields array cannot be empty');
                this.validationErrors.set(errors);
                return;
            }

            if (parsed.fields.length > 10) {
                errors.push('Maximum 10 fields allowed');
                this.validationErrors.set(errors);
                return;
            }

            parsed.fields.forEach((field: any, index: number) => {
                if (!field.fieldKey || typeof field.fieldKey !== 'string') {
                    errors.push(`Field ${index + 1}: fieldKey is required`);
                }
                if (!field.fieldType || !['text', 'textarea', 'number', 'boolean'].includes(field.fieldType)) {
                    errors.push(`Field ${index + 1}: invalid fieldType`);
                }
            });

            if (errors.length > 0) {
                this.validationErrors.set(errors);
                return;
            }

            this.validationErrors.set([]);
            this.parsedFields.set(parsed.fields);
        } catch (error) {
            this.validationErrors.set(['Invalid JSON syntax']);
            this.parsedFields.set([]);
        }
    }

    submit(): void {
        this.onSave();
    }

    protected onSave(): void {
        const fields = this.parsedFields();
        if (fields.length === 0) {
            this.#notify.warning('admin.components.entryFields.noFieldsToImport');
            return;
        }

        this.setSubmitting(true);
        this.#service.importSchema(this.data!.componentTypeId, { fields })
            .pipe(take(1), takeUntil(this.destroy$))
            .subscribe({
                next: (result) => {
                    this.setSubmitting(false);
                    this.importResult.set(result);

                    if (result.failedCount === 0) {
                        this.#notify.success('admin.components.entryFields.importSuccess');
                        this.close(true);
                    } else {
                        this.#notify.warning('admin.components.entryFields.importPartialSuccess');
                    }
                },
                error: () => {
                    this.setSubmitting(false);
                    this.#notify.alert('admin.components.entryFields.importFailed');
                }
            });
    }
}

