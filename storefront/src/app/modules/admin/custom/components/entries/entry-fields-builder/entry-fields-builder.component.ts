import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, Input, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { TranslocoModule } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { SpaEmptyStateComponent } from 'app/shared/components/custom-ui/spa-empty-state/spa-empty-state.component';
import { take } from 'rxjs';
import { EntryFieldDefinitionResponse } from '../../models/component-entry.types';
import { EntryFieldService } from '../../services/entry-field.service';
import { EntryFieldDialogComponent } from '../entry-field-dialog/entry-field-dialog.component';
import { EntryFieldImportDialogComponent } from '../entry-field-import-dialog/entry-field-import-dialog.component';

@Component({
    selector: 'spa-entry-fields-builder',
    templateUrl: './entry-fields-builder.component.html',
    styleUrls: ['./entry-fields-builder.component.scss'],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule,
        MatTableModule,
        MatProgressSpinnerModule,
        TranslocoModule,
        SpaEmptyStateComponent
    ]
})
export class EntryFieldsBuilderComponent implements OnInit {
    #service = inject(EntryFieldService);
    #notify = inject(NotificationService);
    #dialog = inject(MatDialog);

    @Input({ required: true }) componentTypeId!: number;

    fields = signal<EntryFieldDefinitionResponse[]>([]);
    isLoading = signal<boolean>(false);

    displayedColumns = ['fieldKey', 'fieldType', 'isRequired'];

    ngOnInit(): void {
        this.loadFields();
    }

    loadFields(): void {
        this.isLoading.set(true);
        this.#service.getFields(this.componentTypeId)
            .pipe(take(1))
            .subscribe({
                next: (fields) => {
                    this.fields.set(fields);
                    this.isLoading.set(false);
                },
                error: () => {
                    this.#notify.alert('admin.components.entryFields.loadFailed');
                    this.isLoading.set(false);
                }
            });
    }

    openAddFieldDialog(): void {
        const dialogRef = this.#dialog.open(EntryFieldDialogComponent, {
            width: '600px',
            disableClose: true
        });

        dialogRef.afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (!result) return;

                this.#service.addField(this.componentTypeId, result)
                    .pipe(take(1))
                    .subscribe({
                        next: () => {
                            this.#notify.success('admin.components.entryFields.addSuccess');
                            this.loadFields();
                        },
                        error: () => {
                            this.#notify.alert('admin.components.entryFields.addFailed');
                        }
                    });
            });
    }

    openImportDialog(): void {
        const dialogRef = this.#dialog.open(EntryFieldImportDialogComponent, {
            width: '800px',
            maxHeight: '90vh',
            disableClose: true,
            data: { componentTypeId: this.componentTypeId }
        });

        dialogRef.afterClosed()
            .pipe(take(1))
            .subscribe((success) => {
                if (success) {
                    this.loadFields();
                }
            });
    }

    exportFields(): void {
        if (this.fields().length === 0) {
            this.#notify.warning('admin.components.entryFields.noFieldsToExport');
            return;
        }

        this.#service.exportSchema(this.componentTypeId)
            .pipe(take(1))
            .subscribe({
                next: (blob) => {
                    const url = window.URL.createObjectURL(blob);
                    const link = document.createElement('a');
                    link.href = url;
                    link.download = `entry-fields-${this.componentTypeId}.json`;
                    link.click();
                    window.URL.revokeObjectURL(url);
                    this.#notify.success('admin.components.entryFields.exportSuccess');
                },
                error: () => {
                    this.#notify.alert('admin.components.entryFields.exportFailed');
                }
            });
    }
}

