import { Component, inject, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { TranslocoModule } from '@jsverse/transloco';
import { ExtendedFieldsSchema } from '../../models/component-library.types';
import { ExtendedFieldsBuilderComponent } from '../extended-fields-builder/extended-fields-builder.component';

@Component({
    selector: 'spa-extended-fields-builder-dialog',
    standalone: true,
    imports: [MatDialogModule, MatButtonModule, ReactiveFormsModule, ExtendedFieldsBuilderComponent, TranslocoModule],
    template: `
        <h2 mat-dialog-title>{{ 'admin.components.extendedFields.manageTitle' | transloco }}</h2>
        <mat-dialog-content>
            <spa-extended-fields-builder [formControl]="schemaControl" />
        </mat-dialog-content>
        <mat-dialog-actions align="end">
            <button mat-button (click)="cancel()">{{ 'admin.common.actions.cancel' | transloco }}</button>
            <button mat-raised-button color="primary" (click)="save()">{{ 'admin.common.actions.save' | transloco }}</button>
        </mat-dialog-actions>
    `,
    styles: [`mat-dialog-content { min-height: 400px; max-height: 70vh; overflow-y: auto; }`]
})
export class ExtendedFieldsBuilderDialogComponent implements OnInit {
    #dialogRef = inject(MatDialogRef<ExtendedFieldsBuilderDialogComponent>);
    #data = inject<ExtendedFieldsSchema | null>(MAT_DIALOG_DATA);

    schemaControl = new FormControl<ExtendedFieldsSchema | null>(null);

    ngOnInit(): void {
        if (this.#data) {
            this.schemaControl.setValue(this.#data);
        }
    }

    cancel(): void {
        this.#dialogRef.close();
    }

    save(): void {
        this.#dialogRef.close(this.schemaControl.value);
    }
}
