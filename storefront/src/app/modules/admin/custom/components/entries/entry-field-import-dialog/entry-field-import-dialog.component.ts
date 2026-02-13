import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaDialogComponent } from '@shared/components/spa-dialog';
import { SpaDialogData } from '@shared/components/spa-dialog-base/spa-dialog-base.types';
import { SpaFormDialog } from '@shared/components/spa-form-dialog';
import { NotificationService } from '@shared/notifications/notification.service';
import { take, takeUntil } from 'rxjs';
import { CreateEntryFieldRequest, ImportResultResponse } from '../../models/component-entry.types';
import { EntryFieldService } from '../../services/entry-field.service';

interface DialogData extends SpaDialogData {
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
        MatTableModule,
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        SpaTextareaComponent,
        SpaDialogComponent
    ]
})
export class EntryFieldImportDialogComponent extends SpaFormDialog<boolean, DialogData> {
    readonly #service = inject(EntryFieldService);
    readonly #notifyService = inject(NotificationService);

    protected form = this.fb.group({
        jsonInput: ['', Validators.required]
    });

    parsedFields = signal<CreateEntryFieldRequest[]>([]);
    validationErrors = signal<string[]>([]);
    importResult = signal<ImportResultResponse | null>(null);

    displayedColumns = ['fieldKey', 'fieldType', 'isRequired'];

    parseJson(): void {
        const jsonInput = this.form.get('jsonInput')?.value;
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

    save(): void {
        const fields = this.parsedFields();
        if (fields.length === 0) {
            this.#notifyService.warning('admin.components.entryFields.noFieldsToImport');
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
                        this.#notifyService.success('admin.components.entryFields.importSuccess');
                        this.close(true);
                    } else {
                        this.#notifyService.warning('admin.components.entryFields.importPartialSuccess');
                    }
                },
                error: () => {
                    this.setSubmitting(false);
                    this.#notifyService.alert('admin.components.entryFields.importFailed');
                }
            });
    }
}
