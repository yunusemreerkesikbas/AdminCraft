import { ChangeDetectionStrategy, Component, inject, OnInit, signal, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { TranslocoModule } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { SpaEmptyStateComponent } from 'app/shared/components/custom-ui/spa-empty-state/spa-empty-state.component';
import { take } from 'rxjs';
import { EntryFieldDefinitionResponse } from '../../models/component-entry.types';
import { EntryFieldService } from '../../services/entry-field.service';
import { EntryFieldDialogComponent } from '../entry-field-dialog/entry-field-dialog.component';

@Component({
    selector: 'spa-entry-fields-builder',
    templateUrl: './entry-fields-builder.component.html',
    styleUrls: ['./entry-fields-builder.component.scss'],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        MatButtonModule,
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

    protected componentTypeId = input.required<number>();

    protected fieldsSig = signal<EntryFieldDefinitionResponse[]>([]);
    protected isLoadingSig = signal<boolean>(false);

    protected displayedColumns = ['fieldKey', 'fieldType'];

    ngOnInit(): void {
        this.loadFields();
    }

    protected loadFields(): void {
        this.isLoadingSig.set(true);
        this.#service.getFields(this.componentTypeId())
            .pipe(take(1))
            .subscribe({
                next: (fields) => {
                    this.fieldsSig.set(fields);
                    this.isLoadingSig.set(false);
                },
                error: (error) => {
                    this.#notify.alert(error?.error?.message ?? '');
                    this.isLoadingSig.set(false);
                }
            });
    }

    protected openAddFieldDialog(): void {
        const dialogRef = this.#dialog.open(EntryFieldDialogComponent, {
            width: '600px',
            disableClose: true
        });

        dialogRef.afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (!result) return;

                this.#service.addFieldWithResponse(this.componentTypeId(), result)
                    .pipe(take(1))
                    .subscribe({
                        next: (response) => {
                            this.#notify.success(response.message ?? '');
                            this.loadFields();
                        },
                        error: (error) => {
                            this.#notify.alert(error?.error?.message ?? '');
                        }
                    });
            });
    }
}
