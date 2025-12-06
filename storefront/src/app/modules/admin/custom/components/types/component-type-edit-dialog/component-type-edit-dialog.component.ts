import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { fuseAnimations } from '@fuse/animations';
import { TranslocoModule } from '@jsverse/transloco';
import { BaseCrudDialogComponent } from '@shared/components/base-crud-dialog';
import { CrudDialogData } from '@shared/components/base-dialog';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { EntryFieldsBuilderComponent } from '../../entries/entry-fields-builder/entry-fields-builder.component';
import { COMPONENT_CATEGORIES } from '../../models/component-categories.constants';
import { ComponentTypeDto, UpdateComponentTypeRequest } from '../../models/component-library.types';
import { ComponentTypeService } from '../../services/component-type.service';

interface DialogData extends CrudDialogData<ComponentTypeDto> {
    type: ComponentTypeDto;
}

@Component({
    selector: 'spa-component-type-edit-dialog',
    templateUrl: './component-type-edit-dialog.component.html',
    styleUrls: ['./component-type-edit-dialog.component.scss'],
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
        MatButtonModule,
        TranslocoModule,
        EntryFieldsBuilderComponent,
        SpaInputComponent,
        SpaSelectComponent
    ]
})
export class ComponentTypeEditDialogComponent extends BaseCrudDialogComponent<ComponentTypeDto, never, UpdateComponentTypeRequest> {
    protected override service = inject(ComponentTypeService);
    protected override form!: FormGroup;
    protected override readonly data!: DialogData;

    categoryOptions = COMPONENT_CATEGORIES;

    override ngOnInit(): void {
        const typeData = this.data.type;
        this.form = this.fb.group({
            name: [typeData.name, Validators.required],
            category: [typeData.category || null]
        });
    }

    save(): void {
        this.onSave();
    }

    protected buildPayload(): UpdateComponentTypeRequest {
        const formValue = this.form.value;
        return {
            name: formValue.name!,
            category: formValue.category || undefined
        };
    }

    protected override onSaveSuccess(result: ComponentTypeDto): void {
        this.notify.success('admin.components.types.success.updated');
        this.close(result);
    }

    protected override onSaveError(error: any): void {
        this.notify.alert('admin.components.types.errors.updateFailed');
    }
}

