import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { fuseAnimations } from '@fuse/animations';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaDialogComponent } from '@shared/components/spa-dialog';
import { SpaFormDialog } from '@shared/components/spa-form-dialog';

@Component({
    selector: 'spa-entry-field-dialog',
    templateUrl: './entry-field-dialog.component.html',
    styleUrls: ['./entry-field-dialog.component.scss'],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    animations: fuseAnimations,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatIconModule,
        TranslocoModule,
        SpaInputComponent,
        SpaSelectComponent,
        SpaDialogComponent
    ]
})
export class EntryFieldDialogComponent extends SpaFormDialog implements OnInit {
    protected form = this.fb.group({
        fieldKey: ['', [Validators.required]],
        fieldType: ['text', Validators.required]
    });

    fieldTypeOptions = [
        { value: 'text', labelKey: 'admin.components.entryFields.types.text' },
        { value: 'textarea', labelKey: 'admin.components.entryFields.types.textarea' },
        { value: 'number', labelKey: 'admin.components.entryFields.types.number' },
        { value: 'boolean', labelKey: 'admin.components.entryFields.types.boolean' },
        { value: 'media', labelKey: 'admin.components.entryFields.types.media' }
    ];

    override ngOnInit(): void {
        super.ngOnInit();
    }

    save(): void {
        if (this.form.invalid) return;

        const formValue = this.form.value;
        const result = {
            fieldKey: formValue.fieldKey!,
            fieldType: formValue.fieldType!
        };

        this.close(result);
    }
}
